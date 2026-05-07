package com.dealercrest.domain;

public enum DomainStatus {

    PENDING_DNS,
    PENDING_CERT_VERIFICATION,
    PENDING_CERT,
    VALIDATING,
    ACTIVE,
    FAILED;

    /**
     * Returns the lowercase string value for PostgreSQL storage.
     */
    public String toDbValue() {
        return name().toLowerCase();
    }

    /**
     * Parses the string value from PostgreSQL back to enum.
     */
    public static DomainStatus fromDbValue(String value) {
        return valueOf(value.toUpperCase());
    }
}