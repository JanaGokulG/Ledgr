package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDto {
    @JsonProperty("Txnid")
    @JsonAlias({"txnid", "txnId", "txnID"})
    private String txnId;

    @JsonProperty("Type")
    @JsonAlias({"type", "TransactionType", "txnType"})
    private String type;

    @JsonProperty("Mode")
    @JsonAlias({"mode", "paymentMode"})
    private String mode;

    @JsonProperty("Amount")
    @JsonAlias({"amount", "TxnAmount"})
    private String amount;

    @JsonProperty("CurrentBalance")
    @JsonAlias({"currentBalance", "Balance", "balance"})
    private String currentBalance;

    @JsonProperty("TransactionTimestamp")
    @JsonAlias({"transactionTimestamp", "Transactiontimestamp", "TransactionDatetime", "transactionDatetime", "txnTimestamp"})
    private String transactionTimestamp;

    @JsonProperty("valueDate")
    @JsonAlias({"ValueDate", "valDate"})
    private String valueDate;

    @JsonProperty("Narration")
    @JsonAlias({"narration", "Description", "description"})
    private String narration;

    @JsonProperty("Reference")
    @JsonAlias({"reference", "refNumber", "referenceId"})
    private String reference;

    @JsonProperty("startDate")
    @JsonAlias({"TransactionsStartDate", "startDate"})
    private String startDate;

    @JsonProperty("endDate")
    @JsonAlias({"TransactionsEndDate", "endDate"})
    private String endDate;

    public TransactionDto() {}

    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(String currentBalance) { this.currentBalance = currentBalance; }

    public String getTransactionTimestamp() { return transactionTimestamp; }
    public void setTransactionTimestamp(String transactionTimestamp) { this.transactionTimestamp = transactionTimestamp; }

    public String getValueDate() { return valueDate; }
    public void setValueDate(String valueDate) { this.valueDate = valueDate; }

    public String getNarration() { return narration; }
    public void setNarration(String narration) { this.narration = narration; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private TransactionDto dto = new TransactionDto();

        public Builder txnId(String txnId) { dto.setTxnId(txnId); return this; }
        public Builder type(String type) { dto.setType(type); return this; }
        public Builder mode(String mode) { dto.setMode(mode); return this; }
        public Builder amount(String amount) { dto.setAmount(amount); return this; }
        public Builder currentBalance(String currentBalance) { dto.setCurrentBalance(currentBalance); return this; }
        public Builder transactionTimestamp(String transactionTimestamp) { dto.setTransactionTimestamp(transactionTimestamp); return this; }
        public Builder valueDate(String valueDate) { dto.setValueDate(valueDate); return this; }
        public Builder narration(String narration) { dto.setNarration(narration); return this; }
        public Builder reference(String reference) { dto.setReference(reference); return this; }
        public Builder startDate(String startDate) { dto.setStartDate(startDate); return this; }
        public Builder endDate(String endDate) { dto.setEndDate(endDate); return this; }

        public TransactionDto build() { return dto; }
    }
}
