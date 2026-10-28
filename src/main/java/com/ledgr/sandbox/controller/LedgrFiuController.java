package com.ledgr.sandbox.controller;

import com.ledgr.entity.AccountEntity;
import com.ledgr.entity.TransactionEntity;
import com.ledgr.repository.AccountRepository;
import com.ledgr.repository.TransactionRepository;
import com.ledgr.sandbox.service.FiuIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/v1/fiu")
@CrossOrigin(origins = "*")
public class LedgrFiuController {

    private static final Logger log = LoggerFactory.getLogger(LedgrFiuController.class);

    private final FiuIngestionService fiuIngestionService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public LedgrFiuController(FiuIngestionService fiuIngestionService,
                              AccountRepository accountRepository,
                              TransactionRepository transactionRepository) {
        this.fiuIngestionService = fiuIngestionService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * One-click FIU sync trigger to ingest RBI AA data into local database
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
     * Get single account by ID or Account Number
     */
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable String accountId) {
        Optional<AccountEntity> acc = accountRepository.findById(accountId);
        if (acc.isEmpty()) {
            acc = accountRepository.findByAccountNumber(accountId);
        }
        if (acc.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Account not found for id/number: " + accountId));
        }
        return ResponseEntity.ok(acc.get());
    }

    /**
     * Get transactions for a specific account
     */
    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionEntity>> getAccountTransactions(@PathVariable String accountId) {
        return ResponseEntity.ok(transactionRepository.findByAccount_IdOrderByTransactionTimestampDesc(accountId));
    }

    /**
     * Cross-account transaction listing with optional search, category, and type filters
     */
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String txnType,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size,
            @RequestParam(required = false, defaultValue = "false") boolean randomOrder,
            @RequestParam(required = false, defaultValue = "0") int delayMs) {

        int pageSize = Math.min(Math.max(size, 1), 200);
        int pageIndex = Math.max(page, 0);

        Page<TransactionEntity> pageResult = transactionRepository.searchTransactions(
                (search != null && !search.isBlank()) ? search.trim() : null,
                (category != null && !category.isBlank()) ? category.trim() : null,
                (txnType != null && !txnType.isBlank()) ? txnType.trim().toUpperCase() : null,
                PageRequest.of(pageIndex, pageSize)
        );

        List<TransactionEntity> transactions = new ArrayList<>(pageResult.getContent());
        if (randomOrder) {
            Collections.shuffle(transactions);
        }

        if (delayMs > 0) {
            try {
                Thread.sleep(Math.min(delayMs, 2000));
            } catch (InterruptedException ignored) {}
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalElements", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("currentPage", pageResult.getNumber());
        response.put("pageSize", pageResult.getSize());
        response.put("randomOrder", randomOrder);
        response.put("transactions", transactions);

        return ResponseEntity.ok(response);
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
