package com.ledgr.sandbox.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_txn_account", columnList = "account_id"),
    @Index(name = "idx_txn_timestamp", columnList = "transaction_timestamp"),
    @Index(name = "idx_txn_category", columnList = "category")
})
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
    private String paymentMode; // UPI, CARD, ATM, FT, CASH, OTHERS

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

    // Student FinTech Enriched Fields
    @Column(name = "category", length = 64)
    private String category; // FOOD_LIFESTYLE, SUBSCRIPTIONS, RENT_HOUSING, INVESTMENTS, etc.

    @Column(name = "merchant_payee", length = 128)
    private String merchantPayee; // Simpl, Swiggy, Amazon, Ola Money, Kotak, etc.

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public TransactionEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public AccountEntity getAccount() { return account; }
    public void setAccount(AccountEntity account) { this.account = account; }

    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }

    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfterTxn() { return balanceAfterTxn; }
    public void setBalanceAfterTxn(BigDecimal balanceAfterTxn) { this.balanceAfterTxn = balanceAfterTxn; }

    public String getTransactionTimestamp() { return transactionTimestamp; }
    public void setTransactionTimestamp(String transactionTimestamp) { this.transactionTimestamp = transactionTimestamp; }

    public String getValueDate() { return valueDate; }
    public void setValueDate(String valueDate) { this.valueDate = valueDate; }

    public String getNarration() { return narration; }
    public void setNarration(String narration) { this.narration = narration; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMerchantPayee() { return merchantPayee; }
    public void setMerchantPayee(String merchantPayee) { this.merchantPayee = merchantPayee; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private TransactionEntity entity = new TransactionEntity();

        public Builder id(String id) { entity.setId(id); return this; }
        public Builder account(AccountEntity account) { entity.setAccount(account); return this; }
        public Builder txnId(String txnId) { entity.setTxnId(txnId); return this; }
        public Builder txnType(String txnType) { entity.setTxnType(txnType); return this; }
        public Builder paymentMode(String paymentMode) { entity.setPaymentMode(paymentMode); return this; }
        public Builder amount(BigDecimal amount) { entity.setAmount(amount); return this; }
        public Builder balanceAfterTxn(BigDecimal balanceAfterTxn) { entity.setBalanceAfterTxn(balanceAfterTxn); return this; }
        public Builder transactionTimestamp(String transactionTimestamp) { entity.setTransactionTimestamp(transactionTimestamp); return this; }
        public Builder valueDate(String valueDate) { entity.setValueDate(valueDate); return this; }
        public Builder narration(String narration) { entity.setNarration(narration); return this; }
        public Builder referenceNumber(String referenceNumber) { entity.setReferenceNumber(referenceNumber); return this; }
        public Builder category(String category) { entity.setCategory(category); return this; }
        public Builder merchantPayee(String merchantPayee) { entity.setMerchantPayee(merchantPayee); return this; }

        public TransactionEntity build() { return entity; }
    }
}
