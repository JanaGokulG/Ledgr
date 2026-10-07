package com.ledgr.sandbox;

import com.ledgr.sandbox.dto.rebit.AccountDataDto;
import com.ledgr.sandbox.dto.rebit.AccountsWrapperDto;
import com.ledgr.sandbox.service.MockRebitDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class MockRebitDataServiceTest {

    @Autowired
    private MockRebitDataService mockRebitDataService;

    @Test
    @DisplayName("Should successfully load and parse designmode_sample.json with 3 accounts")
    void testLoadMockData() {
        AccountsWrapperDto payload = mockRebitDataService.getFullPayload();
        assertNotNull(payload, "Payload should not be null");
        assertNotNull(payload.getAccounts(), "Accounts list should not be null");
        assertEquals(3, payload.getAccounts().size(), "Must parse exactly 3 accounts from mock data");

        List<AccountDataDto> accounts = payload.getAccounts();
        
        // Account 1: Savings
        AccountDataDto savings = accounts.get(0);
        assertEquals("SAVINGS", savings.getProfile().getAccount().getAcctype());
        assertEquals("DEPOSIT", savings.getProfile().getAccount().getFitype());
        assertEquals("Akshay Kumar", savings.getProfile().getHolders().getHolders().get(0).getName());
        assertFalse(savings.getTransactions().isEmpty(), "Savings account should contain transactions");

        // Account 2: Term Deposit (FD)
        AccountDataDto termDeposit = accounts.get(1);
        assertEquals("TERM-DEPOSIT", termDeposit.getProfile().getAccount().getFitype());
        assertEquals("FIXED", termDeposit.getSummary().getType());
        assertEquals("100000.00", termDeposit.getSummary().getPrincipalAmount());
        assertEquals("105622.00", termDeposit.getSummary().getMaturityAmount());

        // Account 3: Recurring Deposit (RD)
        AccountDataDto recurringDeposit = accounts.get(2);
        assertEquals("RECURRING_DEPOSIT", recurringDeposit.getProfile().getAccount().getFitype());
        assertEquals("RECURRING", recurringDeposit.getSummary().getType());
        assertEquals("5000.00", recurringDeposit.getSummary().getRecurringAmount());
        assertEquals("61693.00", recurringDeposit.getSummary().getMaturityAmount());
    }
}
