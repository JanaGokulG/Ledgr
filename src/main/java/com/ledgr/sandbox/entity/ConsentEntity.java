package com.ledgr.sandbox.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "aa_consents")
public class ConsentEntity {

    @Id
    @Column(name = "consent_id", length = 64)
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // PENDING, ACTIVE, REJECTED, REVOKED, EXPIRED

    @Column(name = "consent_types", length = 256)
    private String consentTypes;

    @Column(name = "fi_types", length = 256)
    private String fiTypes;

    @Column(name = "date_range_from")
    private String dateRangeFrom;

    @Column(name = "date_range_to")
    private String dateRangeTo;

    @Column(name = "redirect_url", length = 512)
    private String redirectUrl;

    @Column(name = "callback_url", length = 512)
    private String callbackUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ConsentEntity() {}

    public ConsentEntity(String id, String customerId, String status, String consentTypes, String fiTypes,
                         String dateRangeFrom, String dateRangeTo, String redirectUrl, String callbackUrl,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.consentTypes = consentTypes;
        this.fiTypes = fiTypes;
        this.dateRangeFrom = dateRangeFrom;
        this.dateRangeTo = dateRangeTo;
        this.redirectUrl = redirectUrl;
        this.callbackUrl = callbackUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getConsentTypes() { return consentTypes; }
    public void setConsentTypes(String consentTypes) { this.consentTypes = consentTypes; }

    public String getFiTypes() { return fiTypes; }
    public void setFiTypes(String fiTypes) { this.fiTypes = fiTypes; }

    public String getDateRangeFrom() { return dateRangeFrom; }
    public void setDateRangeFrom(String dateRangeFrom) { this.dateRangeFrom = dateRangeFrom; }

    public String getDateRangeTo() { return dateRangeTo; }
    public void setDateRangeTo(String dateRangeTo) { this.dateRangeTo = dateRangeTo; }

    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }

    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String customerId;
        private String status;
        private String consentTypes;
        private String fiTypes;
        private String dateRangeFrom;
        private String dateRangeTo;
        private String redirectUrl;
        private String callbackUrl;

        public Builder id(String id) { this.id = id; return this; }
        public Builder customerId(String customerId) { this.customerId = customerId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder consentTypes(String consentTypes) { this.consentTypes = consentTypes; return this; }
        public Builder fiTypes(String fiTypes) { this.fiTypes = fiTypes; return this; }
        public Builder dateRangeFrom(String dateRangeFrom) { this.dateRangeFrom = dateRangeFrom; return this; }
        public Builder dateRangeTo(String dateRangeTo) { this.dateRangeTo = dateRangeTo; return this; }
        public Builder redirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; return this; }
        public Builder callbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; return this; }

        public ConsentEntity build() {
            return new ConsentEntity(id, customerId, status, consentTypes, fiTypes, dateRangeFrom, dateRangeTo, redirectUrl, callbackUrl, null, null);
        }
    }
}
