package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentRequestDto {
    private String customerId; // e.g. "student@setuaa" or mobile number
    private List<String> consentTypes; // PROFILE, SUMMARY, TRANSACTIONS
    private List<String> fiTypes; // DEPOSIT, TERM_DEPOSIT, RECURRING_DEPOSIT
    private DateRangeDto dateRange;
    private String consentExpiry;
    private String redirectUrl;
    private String callbackUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRangeDto {
        private String from; // e.g. 2019-12-01
        private String to;   // e.g. 2021-01-01
    }
}
