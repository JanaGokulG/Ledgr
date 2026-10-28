package com.ledgr.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "fi_type", length = 64)
    private String fiType; // DEPOSITORY, CREDIT, LOAN, INVESTMENT

    @Column(name = "account_type", length = 64)
    private String accountType; // CHECKING, SAVINGS, CD, CREDIT CARD, etc.

    @Column(name = "holder_name")
    private String holderName;

    @Column(name = "holder_pan", length = 32)
    private String holderPan;

    @Column(name = "holder_mobile", length = 32)
    private String holderMobile;

    @Column(name = "holder_email", length = 128)
    private String holderEmail;

    @Column(name = "holder_address", length = 512)
    private String holderAddress;

    @Column(name = "nominee_status", length = 64)
    private String nomineeStatus;

    @Column(name = "ckyc_compliant")
    private Boolean ckycCompliant;

    @Column(name = "current_balance", precision = 19, scale = 4)
    private BigDecimal currentBalance;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "ifsc", length = 32)
    private String ifsc;

    @Column(name = "branch_name", length = 128)
    private String branchName;

    @Column(name = "micr_code", length = 32)
    private String micrCode;

    @Column(name = "opening_date", length = 32)
    private String openingDate;

    @Column(name = "status", length = 32)
    private String status;

    // Term / Recurring Deposit Specific Fields
    @Column(name = "maturity_amount", precision = 19, scale = 4)
    private BigDecimal maturityAmount;

    @Column(name = "maturity_date", length = 32)
    private String maturityDate;

    @Column(name = "interest_rate", precision = 6, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "principal_amount", precision = 19, scale = 4)
    private BigDecimal principalAmount;

    @Column(name = "tenure_days")
    private Integer tenureDays;

    @Column(name = "tenure_months")
    private Integer tenureMonths;

    @Column(name = "compounding_frequency", length = 32)
    private String compoundingFrequency;

    @Column(name = "interest_computation", length = 32)
    private String interestComputation;

    @Column(name = "recurring_amount", precision = 19, scale = 4)
    private BigDecimal recurringAmount;

    @Column(name = "recurring_deposit_day")
    private Integer recurringDepositDay;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<TransactionEntity> transactions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    public AccountEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getFiType() { return fiType; }
    public void setFiType(String fiType) { this.fiType = fiType; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public String getHolderPan() { return holderPan; }
    public void setHolderPan(String holderPan) { this.holderPan = holderPan; }

    public String getHolderMobile() { return holderMobile; }
    public void setHolderMobile(String holderMobile) { this.holderMobile = holderMobile; }

    public String getHolderEmail() { return holderEmail; }
    public void setHolderEmail(String holderEmail) { this.holderEmail = holderEmail; }

    public String getHolderAddress() { return holderAddress; }
    public void setHolderAddress(String holderAddress) { this.holderAddress = holderAddress; }

    public String getNomineeStatus() { return nomineeStatus; }
    public void setNomineeStatus(String nomineeStatus) { this.nomineeStatus = nomineeStatus; }

    public Boolean getCkycCompliant() { return ckycCompliant; }
    public void setCkycCompliant(Boolean ckycCompliant) { this.ckycCompliant = ckycCompliant; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getIfsc() { return ifsc; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getMicrCode() { return micrCode; }
    public void setMicrCode(String micrCode) { this.micrCode = micrCode; }

    public String getOpeningDate() { return openingDate; }
    public void setOpeningDate(String openingDate) { this.openingDate = openingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getMaturityAmount() { return maturityAmount; }
    public void setMaturityAmount(BigDecimal maturityAmount) { this.maturityAmount = maturityAmount; }

    public String getMaturityDate() { return maturityDate; }
    public void setMaturityDate(String maturityDate) { this.maturityDate = maturityDate; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public Integer getTenureDays() { return tenureDays; }
    public void setTenureDays(Integer tenureDays) { this.tenureDays = tenureDays; }

    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }

    public String getCompoundingFrequency() { return compoundingFrequency; }
    public void setCompoundingFrequency(String compoundingFrequency) { this.compoundingFrequency = compoundingFrequency; }

    public String getInterestComputation() { return interestComputation; }
    public void setInterestComputation(String interestComputation) { this.interestComputation = interestComputation; }

    public BigDecimal getRecurringAmount() { return recurringAmount; }
    public void setRecurringAmount(BigDecimal recurringAmount) { this.recurringAmount = recurringAmount; }

    public Integer getRecurringDepositDay() { return recurringDepositDay; }
    public void setRecurringDepositDay(Integer recurringDepositDay) { this.recurringDepositDay = recurringDepositDay; }

    public List<TransactionEntity> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionEntity> transactions) { this.transactions = transactions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private AccountEntity entity = new AccountEntity();

        public Builder id(String id) { entity.setId(id); return this; }
        public Builder accountNumber(String accountNumber) { entity.setAccountNumber(accountNumber); return this; }
        public Builder fiType(String fiType) { entity.setFiType(fiType); return this; }
        public Builder accountType(String accountType) { entity.setAccountType(accountType); return this; }
        public Builder holderName(String holderName) { entity.setHolderName(holderName); return this; }
        public Builder holderPan(String holderPan) { entity.setHolderPan(holderPan); return this; }
        public Builder holderMobile(String holderMobile) { entity.setHolderMobile(holderMobile); return this; }
        public Builder holderEmail(String holderEmail) { entity.setHolderEmail(holderEmail); return this; }
        public Builder holderAddress(String holderAddress) { entity.setHolderAddress(holderAddress); return this; }
        public Builder nomineeStatus(String nomineeStatus) { entity.setNomineeStatus(nomineeStatus); return this; }
        public Builder ckycCompliant(Boolean ckycCompliant) { entity.setCkycCompliant(ckycCompliant); return this; }
        public Builder currentBalance(BigDecimal currentBalance) { entity.setCurrentBalance(currentBalance); return this; }
        public Builder currency(String currency) { entity.setCurrency(currency); return this; }
        public Builder ifsc(String ifsc) { entity.setIfsc(ifsc); return this; }
        public Builder branchName(String branchName) { entity.setBranchName(branchName); return this; }
        public Builder micrCode(String micrCode) { entity.setMicrCode(micrCode); return this; }
        public Builder openingDate(String openingDate) { entity.setOpeningDate(openingDate); return this; }
        public Builder status(String status) { entity.setStatus(status); return this; }
        public Builder maturityAmount(BigDecimal maturityAmount) { entity.setMaturityAmount(maturityAmount); return this; }
        public Builder maturityDate(String maturityDate) { entity.setMaturityDate(maturityDate); return this; }
        public Builder interestRate(BigDecimal interestRate) { entity.setInterestRate(interestRate); return this; }
        public Builder principalAmount(BigDecimal principalAmount) { entity.setPrincipalAmount(principalAmount); return this; }
        public Builder tenureDays(Integer tenureDays) { entity.setTenureDays(tenureDays); return this; }
        public Builder tenureMonths(Integer tenureMonths) { entity.setTenureMonths(tenureMonths); return this; }
        public Builder compoundingFrequency(String compoundingFrequency) { entity.setCompoundingFrequency(compoundingFrequency); return this; }
        public Builder interestComputation(String interestComputation) { entity.setInterestComputation(interestComputation); return this; }
        public Builder recurringAmount(BigDecimal recurringAmount) { entity.setRecurringAmount(recurringAmount); return this; }
        public Builder recurringDepositDay(Integer recurringDepositDay) { entity.setRecurringDepositDay(recurringDepositDay); return this; }
        public Builder transactions(List<TransactionEntity> transactions) { entity.setTransactions(transactions); return this; }

        public AccountEntity build() { return entity; }
    }
}
