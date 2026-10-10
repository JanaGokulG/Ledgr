package com.ledgr.repository;

import com.ledgr.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
    Optional<TransactionEntity> findByTxnId(String txnId);
    boolean existsByTxnId(String txnId);
    List<TransactionEntity> findByAccount_IdOrderByTransactionTimestampDesc(String accountId);
    List<TransactionEntity> findByCategory(String category);

    @Query("SELECT t.category, SUM(t.amount) FROM TransactionEntity t WHERE t.txnType = 'DEBIT' GROUP BY t.category")
    List<Object[]> getExpenseBreakdownByCategory();

    @Query("SELECT COUNT(t) FROM TransactionEntity t")
    long countTransactions();

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.txnType = 'DEBIT'")
    BigDecimal getTotalDebits();

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t WHERE t.txnType = 'CREDIT'")
    BigDecimal getTotalCredits();
}
