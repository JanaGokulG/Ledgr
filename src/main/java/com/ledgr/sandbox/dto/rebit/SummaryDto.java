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
public class SummaryDto {
    // Common / Savings fields
    @JsonProperty("CurrentBalance")
    @JsonAlias({"currentBalance", "currentValue", "Balance", "balance"})
    private String currentBalance;

    @JsonProperty("Currency")
    @JsonAlias("currency")
    private String currency;

    @JsonProperty("ExchangeRate")
    private String exchangeRate;

    @JsonProperty("balanceDateTime")
    private String balanceDateTime;

    @JsonProperty("Type")
    @JsonAlias({"type", "AccountType", "accountType"})
    private String type;

    @JsonProperty("branch")
    @JsonAlias("Branch")
    private String branch;

    @JsonProperty("Facility")
    private String facility;

    @JsonProperty("ifsc")
    @JsonAlias("IFSC")
    private String ifsc;

    @JsonProperty("micrCode")
    private String micrCode;

    @JsonProperty("openingDate")
    private String openingDate;

    @JsonProperty("CurrentODLimit")
    private String currentODLimit;

    @JsonProperty("DrawingLimt")
    private String drawingLimit;

    @JsonProperty("Status")
    @JsonAlias("status")
    private String status;

    @JsonProperty("Pending")
    private PendingDto pending;

    // Term / Recurring Deposit specific fields
    @JsonProperty("maturityAmount")
    private String maturityAmount;

    @JsonProperty("maturityDate")
    private String maturityDate;

    @JsonProperty("description")
    private String description;

    @JsonProperty("interestPayout")
    private String interestPayout;

    @JsonProperty("interestRate")
    private String interestRate;

    @JsonProperty("principalAmount")
    private String principalAmount;

    @JsonProperty("tenureDays")
    private String tenureDays;

    @JsonProperty("tenureMonths")
    private String tenureMonths;

    @JsonProperty("tenureYears")
    private String tenureYears;

    @JsonProperty("interestComputation")
    private String interestComputation;

    @JsonProperty("compoundingFrequency")
    private String compoundingFrequency;

    @JsonProperty("interestPeriodicPayoutAmount")
    private String interestPeriodicPayoutAmount;

    @JsonProperty("interestOnMaturity")
    private String interestOnMaturity;

    @JsonProperty("currentValue")
    private String currentValue;

    // Recurring deposit specific
    @JsonProperty("recurringAmount")
    private String recurringAmount;

    @JsonProperty("recurringDepositDay")
    private String recurringDepositDay;
}
