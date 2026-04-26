package com.alipay.business.biz.service.impl.auth;

import java.time.Instant;
import java.util.Set;

public class JwtClaims {
    /**
     * subject
     */
    private String subject;
    /**
     * userId
     */
    private String userId;
    /**
     * phoneNo
     */
    private String phoneNo;
    /**
     * scopes
     */
    private Set<String> scopes;
    /**
     * issuer
     */
    private String issuer;
    /**
     * audience
     */
    private String audience;
    /**
     * issuedAt
     */
    private Instant issuedAt;
    /**
     * expiresAt
     */
    private Instant expiresAt;


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

}
