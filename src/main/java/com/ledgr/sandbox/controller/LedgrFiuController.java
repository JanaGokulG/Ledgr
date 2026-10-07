package com.ledgr.sandbox.controller;

import com.ledgr.sandbox.entity.AccountEntity;
import com.ledgr.sandbox.entity.TransactionEntity;
import com.ledgr.sandbox.repository.AccountRepository;
import com.ledgr.sandbox.repository.TransactionRepository;
import com.ledgr.sandbox.service.FiuIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/fiu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LedgrFiuController {

    private final FiuIngestionService fiuIngestionService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * One-click FIU sync trigger
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncAccountAggregator(
            @RequestParam(required = false, defaultValue = "student@setuaa") String customerVpa) {
        Map<String, Object> result = fiuIngestionService.executeFullSync(customerVpa);
        return ResponseEntity.ok(result);
    }

    /**
     * List all accounts synchronized into Ledgr database
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<AccountEntity>> getAccounts() {
        return ResponseEntity.ok(accountRepository.findAllWithTransactions());
    }

    /**
     * Get transactions for a specific account
     */
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionEntity>> getAccountTransactions(@PathVariable String accountId) {
        return ResponseEntity.ok(transactionRepository.findByAccount_IdOrderByTransactionTimestampDesc(accountId));
    }

    /**
     * Student Financial Analytics & Runway Overview
     */
    @GetMapping("/analytics/overview")
    public ResponseEntity<Map<String, Object>> getAnalyticsOverview() {
        List<AccountEntity> accounts = accountRepository.findAll();
        
        BigDecimal totalLiquidSavings = BigDecimal.ZERO;
        BigDecimal totalFdAssets = BigDecimal.ZERO;
        BigDecimal totalRdAssets = BigDecimal.ZERO;

        for (AccountEntity acc : accounts) {
            BigDecimal bal = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : BigDecimal.ZERO;
            if ("SAVINGS".equalsIgnoreCase(acc.getAccountType()) || "DEPOSIT".equalsIgnoreCase(acc.getFiType())) {
                totalLiquidSavings = totalLiquidSavings.add(bal);
            } else if ("FIXED".equalsIgnoreCase(acc.getAccountType()) || "TERM-DEPOSIT".equalsIgnoreCase(acc.getFiType())) {
                totalFdAssets = totalFdAssets.add(acc.getMaturityAmount() != null ? acc.getMaturityAmount() : bal);
            } else if ("RECURRING".equalsIgnoreCase(acc.getAccountType()) || "RECURRING_DEPOSIT".equalsIgnoreCase(acc.getFiType())) {
                totalRdAssets = totalRdAssets.add(acc.getMaturityAmount() != null ? acc.getMaturityAmount() : bal);
            }
        }

        BigDecimal totalDebits = transactionRepository.getTotalDebits();
        if (totalDebits == null) totalDebits = BigDecimal.ZERO;

        BigDecimal totalCredits = transactionRepository.getTotalCredits();
        if (totalCredits == null) totalCredits = BigDecimal.ZERO;

        long txnCount = transactionRepository.countTransactions();

        // Estimate monthly burn rate (assuming dataset spans ~6 months from 2019-12 to 2020-06)
        BigDecimal monthlyBurnRate = totalDebits.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
        BigDecimal scholarshipRunwayMonths = monthlyBurnRate.compareTo(BigDecimal.ZERO) > 0
                ? totalLiquidSavings.divide(monthlyBurnRate, 1, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(999);

        // Expense breakdown
        List<Object[]> breakdown = transactionRepository.getExpenseBreakdownByCategory();
        Map<String, BigDecimal> categoryExpenses = new LinkedHashMap<>();
        for (Object[] row : breakdown) {
            String cat = (String) row[0];
            BigDecimal sum = (BigDecimal) row[1];
            categoryExpenses.put(cat, sum);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalAccountsCount", accounts.size());
        summary.put("totalTransactionsCount", txnCount);
        summary.put("totalLiquidBalance", totalLiquidSavings);
        summary.put("totalFixedDepositAssets", totalFdAssets);
        summary.put("totalRecurringDepositAssets", totalRdAssets);
        summary.put("totalNetWorth", totalLiquidSavings.add(totalFdAssets).add(totalRdAssets));
        summary.put("totalDebits", totalDebits);
        summary.put("totalCredits", totalCredits);
        summary.put("estimatedMonthlyBurnRate", monthlyBurnRate);
        summary.put("scholarshipRunwayMonths", scholarshipRunwayMonths);
        summary.put("expenseBreakdownByCategory", categoryExpenses);

        return ResponseEntity.ok(summary);
    }
}
