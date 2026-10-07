package com.ledgr.sandbox.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "fi_type", length = 64)
    private String fiType; // DEPOSIT, TERM-DEPOSIT, RECURRING_DEPOSIT

    @Column(name = "account_type", length = 64)
    private String accountType; // SAVINGS, FIXED, RECURRING, DEFAULT

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
    @Builder.Default
    private List<TransactionEntity> transactions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
}
