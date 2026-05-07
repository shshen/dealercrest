package com.dealercrest.domain;

import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AcmeClient {

    private static final Logger LOG = Logger.getLogger(AcmeClient.class.getName());

    private static final String ACME_STAGING = "acme://letsencrypt.org/staging";
    private static final String ACME_PRODUCTION = "acme://letsencrypt.org";

    private final AcmeChallengeStore challengeStore;
    private final AcmeCertStore certStore;
    private final AcmeAccountStore accountStore;
    private final TlsCertRepository domainRepository;
    private final String acmeServerUrl;

    public AcmeClient(AcmeChallengeStore challengeStore,
            AcmeCertStore certStore,
            AcmeAccountStore accountStore,
            TlsCertRepository domainRepository,
            boolean production) {
        this.challengeStore = challengeStore;
        this.certStore = certStore;
        this.accountStore = accountStore;
        this.domainRepository = domainRepository;
        this.acmeServerUrl = production ? ACME_PRODUCTION : ACME_STAGING;
    }

    /**
     * Phase 1 — called when status = PENDING_CERT.
     * Places ACME order, publishes challenge token, saves URLs to DB, moves to
     * VALIDATING.
     * Returns immediately — no blocking.
     */
    public void startIssuance(DealerDomain dealer) throws Exception {
        String domain = dealer.getDomain();
        long dealerId = dealer.getDealerId();

        LOG.info("Starting ACME issuance for: " + domain);

        Session session = new Session(acmeServerUrl);
        KeyPair accountKeyPair = accountStore.loadOrCreateAccountKeyPair();
        Login login = buildLogin(session, accountKeyPair);
        Order order = login.getAccount().newOrder().domain(domain).create();

        Authorization auth = order.getAuthorizations().iterator().next();

        Optional<Http01Challenge> challengeOpt = auth.findChallenge(Http01Challenge.class);
        if (!challengeOpt.isPresent()) {
            domainRepository.updateStatusWithError(dealerId, DomainStatus.FAILED,
                    "No HTTP-01 challenge available for: " + domain);
            return;
        }

        Http01Challenge challenge = challengeOpt.get();

        challengeStore.saveChallenge(
                challenge.getToken(),
                challenge.getAuthorization(),
                dealerId);

        challenge.trigger();

        domainRepository.updateAcmeUrls(
                dealerId,
                order.getLocation().toString(),
                auth.getLocation().toString());

        LOG.info("Challenge triggered for: " + domain + ", moving to VALIDATING");
    }

    /**
     * Phase 2 — called each tick when status = VALIDATING.
     * Re-attaches to existing order, checks status, finalizes if ready.
     * Returns immediately — no blocking.
     */
    public void checkIssuance(DealerDomain dealer) throws Exception {
        String domain = dealer.getDomain();
        long dealerId = dealer.getDealerId();

        Session session = new Session(acmeServerUrl);
        KeyPair accountKeyPair = accountStore.loadOrCreateAccountKeyPair();
        Login login = buildLogin(session, accountKeyPair);

        Order order = login.bindOrder(URI.create(dealer.getOrderUrl()).toURL());
        Authorization auth = login.bindAuthorization(URI.create(dealer.getChallengeUrl()).toURL());

        auth.fetch();

        if (auth.getStatus() == Status.INVALID) {
            cleanupChallenge(auth, dealerId);
            domainRepository.updateStatusWithError(dealerId, DomainStatus.FAILED,
                    "ACME challenge validation failed for: " + domain);
            LOG.warning("Challenge INVALID for: " + domain);
            return;
        }

        if (auth.getStatus() != Status.VALID) {
            logRetryAfter(auth.getRetryAfter(), "Auth", domain);
            return;
        }

        cleanupChallenge(auth, dealerId);

        order.fetch();

        if (order.getStatus() == Status.INVALID) {
            domainRepository.updateStatusWithError(dealerId, DomainStatus.FAILED,
                    "ACME order failed for: " + domain);
            return;
        }

        if (order.getStatus() == Status.READY) {
            submitCsr(order, dealerId, domain);
            return;
        }

        if (order.getStatus() == Status.VALID) {
            downloadAndStoreCert(order, dealerId, domain);
            return;
        }

        // PROCESSING or PENDING — come back next tick
        logRetryAfter(order.getRetryAfter(), "Order", domain);
    }

    private void submitCsr(Order order, long dealerId, String domain) throws Exception {
        KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

        KeyStore pendingKeyStore = KeyStore.getInstance("PKCS12");
        pendingKeyStore.load(null, null);
        pendingKeyStore.setKeyEntry(
                "key",
                domainKeyPair.getPrivate(),
                certStore.getKeystorePassword(),
                null);
        certStore.savePendingKeyStore(dealerId, pendingKeyStore);

        order.execute(domainKeyPair);

        LOG.info("CSR submitted for: " + domain + ", waiting for order to become VALID");
    }

    private void downloadAndStoreCert(Order order, long dealerId, String domain) throws Exception {
        Certificate certificate = order.getCertificate();

        KeyStore pendingKeyStore = certStore.loadPendingKeyStore(dealerId);
        PrivateKey privateKey = (PrivateKey) pendingKeyStore.getKey(
                "key", certStore.getKeystorePassword());

        List<X509Certificate> chain = certificate.getCertificateChain();
        X509Certificate[] chainArray = chain.toArray(new X509Certificate[0]);

        KeyStore finalKeyStore = KeyStore.getInstance("PKCS12");
        finalKeyStore.load(null, null);
        finalKeyStore.setKeyEntry("tls", privateKey, certStore.getKeystorePassword(), chainArray);

        certStore.saveCert(dealerId, domain, finalKeyStore);
        certStore.deletePendingKeyStore(dealerId);
        domainRepository.markActive(dealerId);

        LOG.info("Certificate issued and ACTIVE for: " + domain);
    }

    private void cleanupChallenge(Authorization auth, long dealerId) {
        Optional<Http01Challenge> challengeOpt = auth.findChallenge(Http01Challenge.class);
        if (challengeOpt.isPresent()) {
            try {
                challengeStore.deleteChallenge(challengeOpt.get().getToken());
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to delete challenge token for dealer: " + dealerId, e);
            }
        }
    }

    private void logRetryAfter(Optional<Instant> retryAfter, String label, String domain) {
        if (retryAfter.isPresent()) {
            LOG.info(label + " retry after: " + retryAfter.get() + " for: " + domain);
        }
    }

    private Login buildLogin(Session session, KeyPair accountKeyPair) throws AcmeException {
        return new AccountBuilder()
                .agreeToTermsOfService()
                .useKeyPair(accountKeyPair)
                .createLogin(session);
    }
}
