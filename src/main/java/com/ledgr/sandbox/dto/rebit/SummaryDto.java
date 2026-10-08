package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SummaryDto {
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

    @JsonProperty("recurringAmount")
    private String recurringAmount;

    @JsonProperty("recurringDepositDay")
    private String recurringDepositDay;

    public SummaryDto() {}

    public String getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(String currentBalance) { this.currentBalance = currentBalance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(String exchangeRate) { this.exchangeRate = exchangeRate; }

    public String getBalanceDateTime() { return balanceDateTime; }
    public void setBalanceDateTime(String balanceDateTime) { this.balanceDateTime = balanceDateTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getFacility() { return facility; }
    public void setFacility(String facility) { this.facility = facility; }

    public String getIfsc() { return ifsc; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }

    public String getMicrCode() { return micrCode; }
    public void setMicrCode(String micrCode) { this.micrCode = micrCode; }

    public String getOpeningDate() { return openingDate; }
    public void setOpeningDate(String openingDate) { this.openingDate = openingDate; }

    public String getCurrentODLimit() { return currentODLimit; }
    public void setCurrentODLimit(String currentODLimit) { this.currentODLimit = currentODLimit; }

    public String getDrawingLimit() { return drawingLimit; }
    public void setDrawingLimit(String drawingLimit) { this.drawingLimit = drawingLimit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public PendingDto getPending() { return pending; }
    public void setPending(PendingDto pending) { this.pending = pending; }

    public String getMaturityAmount() { return maturityAmount; }
    public void setMaturityAmount(String maturityAmount) { this.maturityAmount = maturityAmount; }

    public String getMaturityDate() { return maturityDate; }
    public void setMaturityDate(String maturityDate) { this.maturityDate = maturityDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInterestPayout() { return interestPayout; }
    public void setInterestPayout(String interestPayout) { this.interestPayout = interestPayout; }

    public String getInterestRate() { return interestRate; }
    public void setInterestRate(String interestRate) { this.interestRate = interestRate; }

    public String getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(String principalAmount) { this.principalAmount = principalAmount; }

    public String getTenureDays() { return tenureDays; }
    public void setTenureDays(String tenureDays) { this.tenureDays = tenureDays; }

    public String getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(String tenureMonths) { this.tenureMonths = tenureMonths; }

    public String getTenureYears() { return tenureYears; }
    public void setTenureYears(String tenureYears) { this.tenureYears = tenureYears; }

    public String getInterestComputation() { return interestComputation; }
    public void setInterestComputation(String interestComputation) { this.interestComputation = interestComputation; }

    public String getCompoundingFrequency() { return compoundingFrequency; }
    public void setCompoundingFrequency(String compoundingFrequency) { this.compoundingFrequency = compoundingFrequency; }

    public String getInterestPeriodicPayoutAmount() { return interestPeriodicPayoutAmount; }
    public void setInterestPeriodicPayoutAmount(String interestPeriodicPayoutAmount) { this.interestPeriodicPayoutAmount = interestPeriodicPayoutAmount; }

    public String getInterestOnMaturity() { return interestOnMaturity; }
    public void setInterestOnMaturity(String interestOnMaturity) { this.interestOnMaturity = interestOnMaturity; }

    public String getCurrentValue() { return currentValue; }
    public void setCurrentValue(String currentValue) { this.currentValue = currentValue; }

    public String getRecurringAmount() { return recurringAmount; }
    public void setRecurringAmount(String recurringAmount) { this.recurringAmount = recurringAmount; }

    public String getRecurringDepositDay() { return recurringDepositDay; }
    public void setRecurringDepositDay(String recurringDepositDay) { this.recurringDepositDay = recurringDepositDay; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private SummaryDto dto = new SummaryDto();

        public Builder currentBalance(String currentBalance) { dto.setCurrentBalance(currentBalance); return this; }
        public Builder currency(String currency) { dto.setCurrency(currency); return this; }
        public Builder exchangeRate(String exchangeRate) { dto.setExchangeRate(exchangeRate); return this; }
        public Builder balanceDateTime(String balanceDateTime) { dto.setBalanceDateTime(balanceDateTime); return this; }
        public Builder type(String type) { dto.setType(type); return this; }
        public Builder branch(String branch) { dto.setBranch(branch); return this; }
        public Builder facility(String facility) { dto.setFacility(facility); return this; }
        public Builder ifsc(String ifsc) { dto.setIfsc(ifsc); return this; }
        public Builder micrCode(String micrCode) { dto.setMicrCode(micrCode); return this; }
        public Builder openingDate(String openingDate) { dto.setOpeningDate(openingDate); return this; }
        public Builder currentODLimit(String currentODLimit) { dto.setCurrentODLimit(currentODLimit); return this; }
        public Builder drawingLimit(String drawingLimit) { dto.setDrawingLimit(drawingLimit); return this; }
        public Builder status(String status) { dto.setStatus(status); return this; }
        public Builder pending(PendingDto pending) { dto.setPending(pending); return this; }
        public Builder maturityAmount(String maturityAmount) { dto.setMaturityAmount(maturityAmount); return this; }
        public Builder maturityDate(String maturityDate) { dto.setMaturityDate(maturityDate); return this; }
        public Builder description(String description) { dto.setDescription(description); return this; }
        public Builder interestPayout(String interestPayout) { dto.setInterestPayout(interestPayout); return this; }
        public Builder interestRate(String interestRate) { dto.setInterestRate(interestRate); return this; }
        public Builder principalAmount(String principalAmount) { dto.setPrincipalAmount(principalAmount); return this; }
        public Builder tenureDays(String tenureDays) { dto.setTenureDays(tenureDays); return this; }
        public Builder tenureMonths(String tenureMonths) { dto.setTenureMonths(tenureMonths); return this; }
        public Builder tenureYears(String tenureYears) { dto.setTenureYears(tenureYears); return this; }
        public Builder interestComputation(String interestComputation) { dto.setInterestComputation(interestComputation); return this; }
        public Builder compoundingFrequency(String compoundingFrequency) { dto.setCompoundingFrequency(compoundingFrequency); return this; }
        public Builder interestPeriodicPayoutAmount(String interestPeriodicPayoutAmount) { dto.setInterestPeriodicPayoutAmount(interestPeriodicPayoutAmount); return this; }
        public Builder interestOnMaturity(String interestOnMaturity) { dto.setInterestOnMaturity(interestOnMaturity); return this; }
        public Builder currentValue(String currentValue) { dto.setCurrentValue(currentValue); return this; }
        public Builder recurringAmount(String recurringAmount) { dto.setRecurringAmount(recurringAmount); return this; }
        public Builder recurringDepositDay(String recurringDepositDay) { dto.setRecurringDepositDay(recurringDepositDay); return this; }

        public SummaryDto build() { return dto; }
    }
}
