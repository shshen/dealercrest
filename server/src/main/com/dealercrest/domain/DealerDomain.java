package com.dealercrest.domain;

import java.time.Instant;

public class DealerDomain {
    private long dealerId;
    private String domain;
    private String status;
    private String orderUrl;
    private String challengeUrl;
    private int attemptCount;
    private Instant lastChecked;
    private String errorMessage;

    public long getDealerId() {
        return dealerId;
    }

    public String getDomain() {
        return domain;
    }

    public String getStatus() {
        return status;
    }

    public String getOrderUrl() {
        return orderUrl;
    }

    public String getChallengeUrl() {
        return challengeUrl;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getLastChecked() {
        return lastChecked;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setDealerId(long v) {
        this.dealerId = v;
    }

    public void setDomain(String v) {
        this.domain = v;
    }

    public void setStatus(String v) {
        this.status = v;
    }

    public void setOrderUrl(String v) {
        this.orderUrl = v;
    }

    public void setChallengeUrl(String v) {
        this.challengeUrl = v;
    }

    public void setAttemptCount(int v) {
        this.attemptCount = v;
    }

    public void setLastChecked(Instant v) {
        this.lastChecked = v;
    }

    public void setErrorMessage(String v) {
        this.errorMessage = v;
    }
}
