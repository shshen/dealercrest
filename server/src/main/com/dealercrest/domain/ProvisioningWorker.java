package com.dealercrest.domain;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dealercrest.LeaderElectionTask;

public class ProvisioningWorker implements Runnable {

    private static final Logger LOG = Logger.getLogger(ProvisioningWorker.class.getName());

    private final LeaderElectionTask     electionTask;
    private final TlsCertRepository domainRepository;
    private final AcmeClient             acmeClient;
    private final String                 ourPublicIp;

    public ProvisioningWorker(LeaderElectionTask electionTask,
                                  TlsCertRepository domainRepository,
                                  AcmeClient acmeClient,
                                  String ourPublicIp) {
        this.electionTask     = electionTask;
        this.domainRepository = domainRepository;
        this.acmeClient       = acmeClient;
        this.ourPublicIp      = ourPublicIp;
    }

    @Override
    public void run() {
        if (!electionTask.isLeader()) {
            return;
        }
        try {
            processPendingCertVerification();
            processPendingCert();
            processValidating();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "CertProvisioningWorker tick failed", e);
        }
    }

    // Step 1 — check DNS, advance to PENDING_CERT if resolved
    private void processPendingCertVerification() throws Exception {
        List<DealerDomain> domains = domainRepository.findByStatus(
            DomainStatus.PENDING_CERT_VERIFICATION
        );
        for (DealerDomain dealer : domains) {
            if (isDnsPointingToUs(dealer.getDomain())) {
                LOG.info("DNS verified for: " + dealer.getDomain());
                domainRepository.updateStatus(dealer.getDealerId(), DomainStatus.PENDING_CERT);
            } else {
                LOG.info("DNS not yet resolved for: " + dealer.getDomain());
                domainRepository.updateStatusWithError(
                    dealer.getDealerId(),
                    DomainStatus.PENDING_DNS,
                    "DNS A record does not point to " + ourPublicIp
                );
            }
        }
    }

    // Step 2 — start ACME issuance, move to VALIDATING
    private void processPendingCert() throws Exception {
        List<DealerDomain> domains = domainRepository.findByStatus(DomainStatus.PENDING_CERT);
        for (DealerDomain dealer : domains) {
            try {
                acmeClient.startIssuance(dealer);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to start issuance for: " + dealer.getDomain(), e);
                domainRepository.updateStatusWithError(
                    dealer.getDealerId(), DomainStatus.FAILED, e.getMessage()
                );
            }
        }
    }

    // Step 3 — check ACME progress, finalize if ready
    private void processValidating() throws Exception {
        List<DealerDomain> domains = domainRepository.findByStatus(DomainStatus.VALIDATING);
        for (DealerDomain dealer : domains) {
            try {
                acmeClient.checkIssuance(dealer);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to check issuance for: " + dealer.getDomain(), e);
                domainRepository.updateStatusWithError(
                    dealer.getDealerId(), DomainStatus.FAILED, e.getMessage()
                );
            }
        }
    }

    private boolean isDnsPointingToUs(String domain) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(domain);
            for (InetAddress address : addresses) {
                if (address.getHostAddress().equals(ourPublicIp)) {
                    return true;
                }
            }
            return false;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}