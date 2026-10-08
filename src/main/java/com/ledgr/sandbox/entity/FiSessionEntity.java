package com.ledgr.sandbox.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "aa_fi_sessions")
public class FiSessionEntity {

    @Id
    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "consent_id", nullable = false, length = 64)
    private String consentId;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // PENDING, READY, COMPLETED, FAILED

    @Column(name = "date_range_from")
    private String dateRangeFrom;

    @Column(name = "date_range_to")
    private String dateRangeTo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public FiSessionEntity() {}

    public FiSessionEntity(String sessionId, String consentId, String status, String dateRangeFrom, String dateRangeTo, LocalDateTime createdAt) {
        this.sessionId = sessionId;
        this.consentId = consentId;
        this.status = status;
        this.dateRangeFrom = dateRangeFrom;
        this.dateRangeTo = dateRangeTo;
        this.createdAt = createdAt;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDateRangeFrom() { return dateRangeFrom; }
    public void setDateRangeFrom(String dateRangeFrom) { this.dateRangeFrom = dateRangeFrom; }

    public String getDateRangeTo() { return dateRangeTo; }
    public void setDateRangeTo(String dateRangeTo) { this.dateRangeTo = dateRangeTo; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sessionId;
        private String consentId;
        private String status;
        private String dateRangeFrom;
        private String dateRangeTo;

        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public Builder consentId(String consentId) { this.consentId = consentId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder dateRangeFrom(String dateRangeFrom) { this.dateRangeFrom = dateRangeFrom; return this; }
        public Builder dateRangeTo(String dateRangeTo) { this.dateRangeTo = dateRangeTo; return this; }

        public FiSessionEntity build() {
            return new FiSessionEntity(sessionId, consentId, status, dateRangeFrom, dateRangeTo, null);
        }
    }
}
