package com.ledgr;

import com.ledgr.service.BankingSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class BankingSyncServiceTest {

    @Autowired
    private BankingSyncService bankingSyncService;

    @Test
    @DisplayName("Should successfully authenticate and fetch accounts from Banking Sandbox API")
    void testBankingAccountsFetch() {
        String token = bankingSyncService.getOrGenerateAccessToken(false);
        assertNotNull(token, "Access token should not be null");
        assertTrue(token.startsWith("access-sandbox-"), "Access token should start with access-sandbox-");

        Map<String, Object> accountsRes = bankingSyncService.getAccounts(token);
        assertNotNull(accountsRes);
        assertEquals(200, accountsRes.get("_meta_http_status"), "Accounts endpoint should return HTTP 200");
        assertTrue(accountsRes.containsKey("accounts"), "Response should contain 'accounts' key");
    }

    @Test
    @DisplayName("Should fetch and ingest live banking data into database with simulated delay")
    void testFetchAndIngestPipeline() {
        Map<String, Object> result = bankingSyncService.fetchAndIngestBankingData();
        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertTrue((int) result.get("accountsPersisted") > 0, "Should persist accounts into DB");
        assertTrue((int) result.get("transactionsPersisted") >= 0, "Should persist transactions into DB");
    }

    @Test
    @DisplayName("Should fetch transaction details and persist each into database")
    void testSyncAndPersistTransactions() {
        Map<String, Object> result = bankingSyncService.syncAndPersistTransactions(null, null);
        assertNotNull(result);
        assertTrue(result.containsKey("transactionsInsertedIntoDb"), "Should report transactions inserted");
        assertEquals("INSERTED", result.get("dbStatus"));
        int inserted = (int) result.get("transactionsInsertedIntoDb");
        assertTrue(inserted >= 0, "Inserted count should be non-negative");
    }
}
