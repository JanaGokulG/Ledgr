package com.ledgr.sandbox;

import com.ledgr.sandbox.entity.AccountEntity;
import com.ledgr.sandbox.repository.AccountRepository;
import com.ledgr.sandbox.repository.TransactionRepository;
import com.ledgr.sandbox.service.FiuIngestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class FiuEndToEndIntegrationTest {

    @Autowired
    private FiuIngestionService fiuIngestionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Should execute full AA sandbox sync and persist accounts & transactions")
    void testEndToEndSync() {
        Map<String, Object> result = fiuIngestionService.executeFullSync("student@setuaa");
        
        assertEquals("SUCCESS", result.get("status"));
        assertEquals(3, result.get("accountsSynchronized"));
        assertTrue((int) result.get("transactionsSynchronized") > 50, "Should have synced over 50 transactions");

        List<AccountEntity> accounts = accountRepository.findAll();
        assertEquals(3, accounts.size(), "Should have 3 bank accounts in DB");

        long txnCount = transactionRepository.countTransactions();
        assertTrue(txnCount > 50, "Transaction count in DB should match");

        BigDecimal totalDebits = transactionRepository.getTotalDebits();
        assertNotNull(totalDebits);
        assertTrue(totalDebits.compareTo(BigDecimal.ZERO) > 0);
    }
}
