package com.ledgr.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_txn_account", columnList = "account_id"),
    @Index(name = "idx_txn_timestamp", columnList = "transaction_timestamp"),
    @Index(name = "idx_txn_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonBackReference
    private AccountEntity account;

    @Column(name = "txn_id", length = 64)
    private String txnId;

    @Column(name = "txn_type", length = 32)
    private String txnType; // DEBIT, CREDIT, OPENING, INSTALLMENT

    @Column(name = "payment_mode", length = 32)
    private String paymentMode; // UPI, CARD, ATM, FT, CASH, ONLINE, OTHERS

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after_txn", precision = 19, scale = 4)
    private BigDecimal balanceAfterTxn;

    @Column(name = "transaction_timestamp")
    private String transactionTimestamp;

    @Column(name = "value_date")
    private String valueDate;

    @Column(name = "narration", length = 512)
    private String narration;

    @Column(name = "reference_number", length = 64)
    private String referenceNumber;

    @Column(name = "category", length = 64)
    private String category; // FOOD_LIFESTYLE, SUBSCRIPTIONS, RENT_HOUSING, INVESTMENTS, etc.

    @Column(name = "merchant_payee", length = 128)
    private String merchantPayee;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
