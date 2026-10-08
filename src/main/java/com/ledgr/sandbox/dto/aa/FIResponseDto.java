package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FIResponseDto {
    private String sessionId;
    private String consentId;
    private String status;
    private String timestamp = Instant.now().toString();

    public FIResponseDto() {}

    public FIResponseDto(String sessionId, String consentId, String status, String timestamp) {
        this.sessionId = sessionId;
        this.consentId = consentId;
        this.status = status;
        this.timestamp = timestamp != null ? timestamp : Instant.now().toString();
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sessionId;
        private String consentId;
        private String status;
        private String timestamp = Instant.now().toString();

        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public Builder consentId(String consentId) { this.consentId = consentId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }

        public FIResponseDto build() {
            return new FIResponseDto(sessionId, consentId, status, timestamp);
        }
    }
}
