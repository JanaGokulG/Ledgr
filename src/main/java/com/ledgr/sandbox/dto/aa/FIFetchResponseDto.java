package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ledgr.sandbox.dto.rebit.AccountsWrapperDto;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FIFetchResponseDto {
    private String sessionId;
    private String consentId;
    private String status;
    private String timestamp = Instant.now().toString();
    private String fiuId;
    private AccountsWrapperDto payload;

    public FIFetchResponseDto() {}

    public FIFetchResponseDto(String sessionId, String consentId, String status,
                              String timestamp, String fiuId, AccountsWrapperDto payload) {
        this.sessionId = sessionId;
        this.consentId = consentId;
        this.status = status;
        this.timestamp = timestamp != null ? timestamp : Instant.now().toString();
        this.fiuId = fiuId;
        this.payload = payload;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getFiuId() { return fiuId; }
    public void setFiuId(String fiuId) { this.fiuId = fiuId; }

    public AccountsWrapperDto getPayload() { return payload; }
    public void setPayload(AccountsWrapperDto payload) { this.payload = payload; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String sessionId;
        private String consentId;
        private String status;
        private String timestamp = Instant.now().toString();
        private String fiuId;
        private AccountsWrapperDto payload;

        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public Builder consentId(String consentId) { this.consentId = consentId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder timestamp(String timestamp) { this.timestamp = timestamp; return this; }
        public Builder fiuId(String fiuId) { this.fiuId = fiuId; return this; }
        public Builder payload(AccountsWrapperDto payload) { this.payload = payload; return this; }

        public FIFetchResponseDto build() {
            return new FIFetchResponseDto(sessionId, consentId, status, timestamp, fiuId, payload);
        }
    }
}
