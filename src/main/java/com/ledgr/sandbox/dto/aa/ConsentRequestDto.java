package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentRequestDto {
    private String customerId; // e.g. "student@setuaa" or mobile number
    private List<String> consentTypes; // PROFILE, SUMMARY, TRANSACTIONS
    private List<String> fiTypes; // DEPOSIT, TERM_DEPOSIT, RECURRING_DEPOSIT
    private DateRangeDto dateRange;
    private String consentExpiry;
    private String redirectUrl;
    private String callbackUrl;

    public ConsentRequestDto() {}

    public ConsentRequestDto(String customerId, List<String> consentTypes, List<String> fiTypes,
                             DateRangeDto dateRange, String consentExpiry, String redirectUrl, String callbackUrl) {
        this.customerId = customerId;
        this.consentTypes = consentTypes;
        this.fiTypes = fiTypes;
        this.dateRange = dateRange;
        this.consentExpiry = consentExpiry;
        this.redirectUrl = redirectUrl;
        this.callbackUrl = callbackUrl;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public List<String> getConsentTypes() { return consentTypes; }
    public void setConsentTypes(List<String> consentTypes) { this.consentTypes = consentTypes; }

    public List<String> getFiTypes() { return fiTypes; }
    public void setFiTypes(List<String> fiTypes) { this.fiTypes = fiTypes; }

    public DateRangeDto getDateRange() { return dateRange; }
    public void setDateRange(DateRangeDto dateRange) { this.dateRange = dateRange; }

    public String getConsentExpiry() { return consentExpiry; }
    public void setConsentExpiry(String consentExpiry) { this.consentExpiry = consentExpiry; }

    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }

    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String customerId;
        private List<String> consentTypes;
        private List<String> fiTypes;
        private DateRangeDto dateRange;
        private String consentExpiry;
        private String redirectUrl;
        private String callbackUrl;

        public Builder customerId(String customerId) { this.customerId = customerId; return this; }
        public Builder consentTypes(List<String> consentTypes) { this.consentTypes = consentTypes; return this; }
        public Builder fiTypes(List<String> fiTypes) { this.fiTypes = fiTypes; return this; }
        public Builder dateRange(DateRangeDto dateRange) { this.dateRange = dateRange; return this; }
        public Builder consentExpiry(String consentExpiry) { this.consentExpiry = consentExpiry; return this; }
        public Builder redirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; return this; }
        public Builder callbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; return this; }

        public ConsentRequestDto build() {
            return new ConsentRequestDto(customerId, consentTypes, fiTypes, dateRange, consentExpiry, redirectUrl, callbackUrl);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DateRangeDto {
        private String from;
        private String to;

        public DateRangeDto() {}
        public DateRangeDto(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }

        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String from;
            private String to;
            public Builder from(String from) { this.from = from; return this; }
            public Builder to(String to) { this.to = to; return this; }
            public DateRangeDto build() { return new DateRangeDto(from, to); }
        }
    }
}
