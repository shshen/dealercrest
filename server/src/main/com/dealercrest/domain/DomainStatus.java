package com.dealercrest.domain;

public final class DomainStatus {
    public static final String PENDING_DNS               = "PENDING_DNS";
    public static final String PENDING_CERT_VERIFICATION = "PENDING_CERT_VERIFICATION";
    public static final String PENDING_CERT              = "PENDING_CERT";
    public static final String VALIDATING                = "VALIDATING";
    public static final String ACTIVE                    = "ACTIVE";
    public static final String FAILED                    = "FAILED";

    private DomainStatus() {}
}