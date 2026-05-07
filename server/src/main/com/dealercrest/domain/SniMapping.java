package com.dealercrest.domain;

import io.netty.handler.ssl.SslContext;
import io.netty.util.AsyncMapping;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Thread-safe SNI domain -> SslContext mapping.
 * Registered with Netty's SniHandler at server startup.
 * Updated at runtime as dealer certs are issued or renewed.
 */
public class SniMapping implements AsyncMapping<String, SslContext> {

    private static final Logger LOG = Logger.getLogger(SniMapping.class.getName());

    private final ConcurrentHashMap<String, SslContext> mapping;
    private final SslContext defaultSslContext;

    /**
     * @param defaultSslContext fallback context for unrecognized domains
     *                          (e.g. your platform's own wildcard cert)
     */
    public SniMapping(SslContext defaultSslContext) {
        this.mapping = new ConcurrentHashMap<String, SslContext>();
        this.defaultSslContext = defaultSslContext;
    }

    /**
     * Called by Netty on every TLS handshake with the client's SNI hostname.
     * Must be non-blocking — ConcurrentHashMap lookup is safe here.
     */
    @Override
    public Future<SslContext> map(String hostname, Promise<SslContext> promise) {
        if (hostname == null) {
            promise.setSuccess(defaultSslContext);
            return promise;
        }

        SslContext ctx = mapping.get(hostname.toLowerCase());

        if (ctx == null) {
            LOG.fine("No cert found for hostname: " + hostname + ", using default.");
            promise.setSuccess(defaultSslContext);
        } else {
            promise.setSuccess(ctx);
        }

        return promise;
    }

    /**
     * Registers or replaces the SslContext for a domain.
     * Called by SniCertSyncWorker after loading a cert from S3.
     */
    public void put(String domain, SslContext sslContext) {
        mapping.put(domain.toLowerCase(), sslContext);
        LOG.info("SNI mapping updated for domain: " + domain);
    }

    /**
     * Removes the mapping for a domain.
     * Call when a dealer deactivates their custom domain.
     */
    public void remove(String domain) {
        mapping.remove(domain.toLowerCase());
        LOG.info("SNI mapping removed for domain: " + domain);
    }

    public boolean contains(String domain) {
        return mapping.containsKey(domain.toLowerCase());
    }

    public int size() {
        return mapping.size();
    }
}
