package com.dealercrest.domain;

import java.security.KeyStore;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class SniCertSyncWorker implements Runnable {

    private static final Logger LOG = Logger.getLogger(SniCertSyncWorker.class.getName());

    private final TlsCertRepository domainRepository;
    private final AcmeCertStore certStore;
    private final SniMapping sniMapping;

    private Instant lastSyncedAt;

    public SniCertSyncWorker(TlsCertRepository domainRepository,
            AcmeCertStore certStore,
            SniMapping sniMapping) {
        this.domainRepository = domainRepository;
        this.certStore = certStore;
        this.sniMapping = sniMapping;
        this.lastSyncedAt = Instant.EPOCH; // load all active certs on first tick
    }

    @Override
    public void run() {
        try {
            List<DealerDomain> changed = domainRepository.findActivatedAfter(lastSyncedAt);
            for (DealerDomain dealer : changed) {
                reloadCert(dealer);
            }
            lastSyncedAt = Instant.now();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "SniCertSyncWorker tick failed", e);
        }
    }

    private void reloadCert(DealerDomain dealer) {
        try {
            KeyStore keyStore = certStore.loadCert(dealer.getDealerId());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, certStore.getKeystorePassword());

            SslContext sslContext = SslContextBuilder.forServer(kmf).build();
            sniMapping.put(dealer.getDomain(), sslContext);

            LOG.info("SNI cert reloaded for: " + dealer.getDomain());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to reload cert for: " + dealer.getDomain(), e);
        }
    }
}