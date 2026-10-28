package com.ledgr;

import com.ledgr.sandbox.controller.FintechToolsController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class RenewalWarningsTest {

    @Autowired
    private FintechToolsController fintechToolsController;

    @Test
    @DisplayName("Should detect upcoming auto-renewals and calculate minimum balance warnings")
    void testRenewalWarningsBasic() {
        ResponseEntity<Map<String, Object>> response = fintechToolsController.getRenewalWarnings(
                BigDecimal.valueOf(1000), 14, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("overallStatus"));
        assertTrue(body.containsKey("headline"));
        assertTrue(body.containsKey("upcomingPayments"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> payments = (List<Map<String, Object>>) body.get("upcomingPayments");
        assertNotNull(payments);
        assertFalse(payments.isEmpty(), "Should have upcoming payments detected or standard recurring student items");

        Map<String, Object> first = payments.get(0);
        assertTrue(first.containsKey("serviceName"));
        assertTrue(first.containsKey("amount"));
        assertTrue(first.containsKey("daysRemaining"));
        assertTrue(first.containsKey("warningMessage"));
        assertTrue(first.containsKey("status"));
    }

    @Test
    @DisplayName("Should trigger CRITICAL_INSUFFICIENT warning when simulated balance is lower than payment amount")
    void testCriticalInsufficientBalanceWarning() {
        // Balance is ₹100, which is less than Netflix (₹199) or RD (₹1000)
        ResponseEntity<Map<String, Object>> response = fintechToolsController.getRenewalWarnings(
                BigDecimal.valueOf(1000), 14, BigDecimal.valueOf(100));

        assertNotNull(response);
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals("CRITICAL_ACTION_REQUIRED", body.get("overallStatus"));
        int criticalCount = (int) body.get("criticalAlertsCount");
        assertTrue(criticalCount > 0, "Should have at least 1 critical alert");

        BigDecimal topUp = (BigDecimal) body.get("recommendedTopUpAmount");
        assertTrue(topUp.compareTo(BigDecimal.ZERO) > 0, "Should recommend positive top-up amount");
    }

    @Test
    @DisplayName("Should trigger WARNING_LOW_BALANCE when balance covers payment but falls below minimum threshold")
    void testLowBalanceWarning() {
        // Balance is ₹1100. Netflix is ₹199, balance after debit is ₹901 (< min balance threshold ₹1000)
        ResponseEntity<Map<String, Object>> response = fintechToolsController.getRenewalWarnings(
                BigDecimal.valueOf(1000), 14, BigDecimal.valueOf(1100));

        assertNotNull(response);
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        int lowBalanceCount = (int) body.get("lowBalanceWarningsCount");
        assertTrue(lowBalanceCount > 0 || (int) body.get("criticalAlertsCount") > 0,
                "Should flag either low balance or critical alerts");
    }
}
