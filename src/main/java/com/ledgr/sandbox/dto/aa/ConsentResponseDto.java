package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentResponseDto {
    private String id;
    private String status;
    private String url;
    private String customerId;
    private List<String> consentTypes;
    private List<String> fiTypes;
    private String createdAt = Instant.now().toString();

    public ConsentResponseDto() {}

    public ConsentResponseDto(String id, String status, String url, String customerId,
                              List<String> consentTypes, List<String> fiTypes, String createdAt) {
        this.id = id;
        this.status = status;
        this.url = url;
        this.customerId = customerId;
        this.consentTypes = consentTypes;
        this.fiTypes = fiTypes;
        this.createdAt = createdAt != null ? createdAt : Instant.now().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public List<String> getConsentTypes() { return consentTypes; }
    public void setConsentTypes(List<String> consentTypes) { this.consentTypes = consentTypes; }

    public List<String> getFiTypes() { return fiTypes; }
    public void setFiTypes(List<String> fiTypes) { this.fiTypes = fiTypes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String status;
        private String url;
        private String customerId;
        private List<String> consentTypes;
        private List<String> fiTypes;
        private String createdAt = Instant.now().toString();

        public Builder id(String id) { this.id = id; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder url(String url) { this.url = url; return this; }
        public Builder customerId(String customerId) { this.customerId = customerId; return this; }
        public Builder consentTypes(List<String> consentTypes) { this.consentTypes = consentTypes; return this; }
        public Builder fiTypes(List<String> fiTypes) { this.fiTypes = fiTypes; return this; }
        public Builder createdAt(String createdAt) { this.createdAt = createdAt; return this; }

        public ConsentResponseDto build() {
            return new ConsentResponseDto(id, status, url, customerId, consentTypes, fiTypes, createdAt);
        }
    }
}
