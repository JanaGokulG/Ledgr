package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDataDto {
    private ProfileDto profile;
    private SummaryDto summary;
    private List<TransactionDto> transactions = new ArrayList<>();

    public AccountDataDto() {}

    public AccountDataDto(ProfileDto profile, SummaryDto summary, List<TransactionDto> transactions) {
        this.profile = profile;
        this.summary = summary;
        this.transactions = transactions != null ? transactions : new ArrayList<>();
    }

    public ProfileDto getProfile() { return profile; }
    public void setProfile(ProfileDto profile) { this.profile = profile; }

    public SummaryDto getSummary() { return summary; }
    public void setSummary(SummaryDto summary) { this.summary = summary; }

    public List<TransactionDto> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionDto> transactions) { this.transactions = transactions; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private ProfileDto profile;
        private SummaryDto summary;
        private List<TransactionDto> transactions = new ArrayList<>();

        public Builder profile(ProfileDto profile) { this.profile = profile; return this; }
        public Builder summary(SummaryDto summary) { this.summary = summary; return this; }
        public Builder transactions(List<TransactionDto> transactions) { this.transactions = transactions; return this; }

        public AccountDataDto build() {
            return new AccountDataDto(profile, summary, transactions);
        }
    }
}
