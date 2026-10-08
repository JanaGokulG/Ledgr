package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PendingDto {
    @JsonProperty("TransactionType")
    private String transactionType;

    @JsonProperty("Amount")
    private String amount;

    public PendingDto() {}

    public PendingDto(String transactionType, String amount) {
        this.transactionType = transactionType;
        this.amount = amount;
    }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String transactionType;
        private String amount;

        public Builder transactionType(String transactionType) { this.transactionType = transactionType; return this; }
        public Builder amount(String amount) { this.amount = amount; return this; }

        public PendingDto build() {
            return new PendingDto(transactionType, amount);
        }
    }
}
