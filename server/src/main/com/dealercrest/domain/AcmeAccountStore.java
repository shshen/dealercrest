package com.dealercrest.domain;

import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.StringReader;
import java.security.KeyPair;
import java.util.logging.Logger;

/**
 * Loads or creates the Let's Encrypt account keypair.
 * Stored once in S3 at: certs/account/account.pem
 * This is shared across all nodes — must never be regenerated once registered.
 */
public class AcmeAccountStore {

    private static final Logger LOG = Logger.getLogger(AcmeAccountStore.class.getName());

    public KeyPair loadOrCreateAccountKeyPair() throws Exception {
        String existing = tryLoadFromS3();
        if (existing != null) {
            return KeyPairUtils.readKeyPair(new StringReader(existing));
        }
        LOG.info("No account keypair found — generating new one.");
        KeyPair keyPair = KeyPairUtils.createKeyPair(2048);
        return keyPair;
    }

    private String tryLoadFromS3() {
        return null;
    }

}
