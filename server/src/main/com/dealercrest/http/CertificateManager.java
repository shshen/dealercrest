package com.dealercrest.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.DomainWildcardMappingBuilder;
import io.netty.util.Mapping;

public class CertificateManager {

    private static final String ALGORITHM_SUN_X509 = "SunX509";
    private static final String ALGORITHM = "ssl.KeyManagerFactory.algorithm";
    private static final String KEYSTORE_TYPE = "JKS";
    private final Mapping<String, SslContext> certMap;
    private static final String KEYSTORE_PASSWORD = System.getProperty("KEYSTORE_PASSWORD");
    private static final String CERT_PASSWORD = System.getProperty("KEYSTORE_PASSWORD");
    private static final Logger logger = Logger.getLogger(CertificateManager.class.getName());

    public CertificateManager() {
        SslContext defaultCxt = buildSslContext("/keystore.pkcs12");
        // SslContext fordCxt = buildSslContext("./domains/ford.dataleading.com/ford.pkcs12");
        // SslContext audiCxt = buildSslContext("./domains/audi.dataleading.com/audi.pkcs12");
        DomainWildcardMappingBuilder<SslContext> builder = new DomainWildcardMappingBuilder<>(defaultCxt);
        // builder.add("ford.dataleading.com", fordCxt);
        // builder.add("audi.dataleading.com", audiCxt);
        this.certMap = builder.build();
    }

    public Mapping<String, SslContext> getMapping() {
        return certMap;
    }

    private SslContext buildSslContext(String keyStore) {
        String algorithm = Security.getProperty(ALGORITHM);
        if (algorithm == null) {
            algorithm = ALGORITHM_SUN_X509;
        }
        KeyStore ks = null;
        InputStream inputStream = null;
        try {
            inputStream = getStream(keyStore);
            ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(inputStream, KEYSTORE_PASSWORD.toCharArray());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot load the keystore file", e);
        } catch (CertificateException e) {
            logger.log(Level.SEVERE, "Cannot get the certificate", e);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Somthing wrong with the SSL algorithm", e);
        } catch (KeyStoreException e) {
            logger.log(Level.SEVERE, "Cannot initialize keystore", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Cannot close keystore file stream ", e);
            }
        }
        try {
            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, CERT_PASSWORD.toCharArray());
            return SslContextBuilder.forServer(kmf).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize the server-side SSLContext", e);
        }
        throw new IllegalArgumentException("failed to init SslContext");
    }

    private InputStream getStream(String keyStore) throws IOException {
        File f = new File(keyStore);
        if (f.exists()) {
            return new FileInputStream(f);
        }
        return this.getClass().getResourceAsStream(keyStore);
    }
}
