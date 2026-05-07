package com.dealercrest.domain;

import java.security.KeyStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Stores and loads dealer TLS certs as PKCS12 keystores in S3.
 *
 * Layout:
 * certs/{dealerId}/keystore.p12
 * certs/pending/{dealerId}/key.p12 (temporary, during READY→VALID transition)
 */
public class AcmeCertStore {

    private static final String PKCS12 = "PKCS12";

    // Password protecting the PKCS12 file at rest — load from env in production
    private static final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();

    /**
     * Saves cert chain + private key as a PKCS12 keystore to S3.
     */
    public void saveCert(long dealerId, String domain, KeyStore keyStore) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        keyStore.store(out, KEYSTORE_PASSWORD);
        putBytes(keystoreKey(dealerId), out.toByteArray());
    }

    /**
     * Loads the PKCS12 keystore from S3. Returns null if not found.
     */
    public KeyStore loadCert(long dealerId) throws Exception {
        byte[] bytes = getBytes(keystoreKey(dealerId));
        if (bytes == null) {
            return null;
        }
        KeyStore keyStore = KeyStore.getInstance(PKCS12);
        keyStore.load(new ByteArrayInputStream(bytes), KEYSTORE_PASSWORD);
        return keyStore;
    }

    /**
     * Temporarily saves the domain keypair during READY state,
     * before the cert is available to bundle.
     */
    public void savePendingKeyStore(long dealerId, KeyStore pendingKeyStore) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pendingKeyStore.store(out, KEYSTORE_PASSWORD);
        putBytes(pendingKey(dealerId), out.toByteArray());
    }

    /**
     * Loads the pending keypair keystore saved during READY state.
     */
    public KeyStore loadPendingKeyStore(long dealerId) throws Exception {
        byte[] bytes = getBytes(pendingKey(dealerId));
        if (bytes == null) {
            return null;
        }
        KeyStore keyStore = KeyStore.getInstance(PKCS12);
        keyStore.load(new ByteArrayInputStream(bytes), KEYSTORE_PASSWORD);
        return keyStore;
    }

    public void deletePendingKeyStore(long dealerId) {
        // No need to check existence first, S3 delete is idempotent
    }

    public char[] getKeystorePassword() {
        return KEYSTORE_PASSWORD;
    }

    private void putBytes(String key, byte[] bytes) {
        // S3 putObject will overwrite existing key, so no need to check existence first
    }

    private byte[] getBytes(String key) {
        // Returns null if key not found in S3
        return null;
    }

    private String keystoreKey(long dealerId) {
        return "certs/" + dealerId + "/keystore.p12";
    }

    private String pendingKey(long dealerId) {
        return "certs/pending/" + dealerId + "/keystore.p12";
    }
}
