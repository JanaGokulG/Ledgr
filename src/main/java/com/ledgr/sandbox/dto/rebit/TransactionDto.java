package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
