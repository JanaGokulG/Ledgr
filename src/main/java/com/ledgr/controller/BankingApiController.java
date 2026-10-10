package com.ledgr.controller;

import com.ledgr.config.BankingProperties;
import com.ledgr.entity.AccountEntity;
import com.ledgr.entity.TransactionEntity;
import com.ledgr.repository.AccountRepository;
import com.ledgr.repository.TransactionRepository;
import com.ledgr.service.BankingSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RestController
@RequestMapping({"/api/v1/banking", "/api/v1/finance", "/api/v1/accounts"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BankingApiController {

    private final BankingSyncService bankingSyncService;
    private final BankingProperties bankingProperties;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Master Sync: Fetches data from Banking API and persists into Database with simulated delays
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncBankingData() {
        Map<String, Object> result = bankingSyncService.fetchAndIngestBankingData();
        return ResponseEntity.ok(result);
    }

    /**
     * Fetches transactions from the Banking API and inserts them directly into the Database,
     * applying the inter-transaction delay between each transaction.
     */
    @PostMapping({"/transactions/sync", "/transactions/fetch-and-save"})
    public ResponseEntity<Map<String, Object>> syncAndSaveTransactions(
            @RequestBody(required = false) Map<String, Object> params) {
        String token = params != null ? (String) params.get("access_token") : null;
        String cursor = params != null ? (String) params.get("cursor") : null;
        Map<String, Object> result = bankingSyncService.syncAndPersistTransactions(token, cursor);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all transactions currently stored in the database
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionEntity>> getAllTransactions() {
        List<TransactionEntity> txns = transactionRepository.findAll();
        txns.sort((a, b) -> Objects.toString(b.getTransactionTimestamp(), "")
                .compareTo(Objects.toString(a.getTransactionTimestamp(), "")));
        return ResponseEntity.ok(txns);
    }

    /**
     * Call any of the specific banking detail endpoints
     */
    @PostMapping({"/endpoint/{endpointSlug}", "/endpoints/{endpointSlug}"})
    public ResponseEntity<Map<String, Object>> callEndpoint(
            @PathVariable String endpointSlug,
            @RequestBody(required = false) Map<String, Object> customParams) {
        Map<String, Object> response = bankingSyncService.dispatchEndpoint(endpointSlug, customParams);
        return ResponseEntity.ok(response);
    }

    /**
     * Generic query runner for custom banking paths
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> queryBanking(
            @RequestParam String endpoint,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = bankingSyncService.dispatchEndpoint(endpoint, body);
        return ResponseEntity.ok(response);
    }

    /**
     * Banking connection status, environment, delay metrics, and DB record counts
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBankingStatus() {
        long accountsCount = accountRepository.count();
        long transactionsCount = transactionRepository.countTransactions();

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("connected", true);
        status.put("environment", bankingProperties.getEnv());
        status.put("baseUrl", bankingProperties.getBaseUrl());
        status.put("clientId", mask(bankingProperties.getClientId()));
        status.put("institutionId", bankingProperties.getInstitutionId());
        status.put("accountsInDatabase", accountsCount);
        status.put("transactionsInDatabase", transactionsCount);
        status.put("transactionDelayMs", bankingProperties.getTransactionDelayMs());
        status.put("supportedEndpointsCount", 9);

        return ResponseEntity.ok(status);
    }

    /**
     * Progressive Transaction Stream: Emits transactions to the frontend one-by-one with a delay between each transaction
     */
    @GetMapping(value = "/transactions/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamTransactions(
            @RequestParam(required = false) Long delayMs) {
        long delay = (delayMs != null && delayMs >= 0) ? delayMs : bankingProperties.getTransactionDelayMs();
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter =
                new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(300_000L);

        java.util.concurrent.Executors.newSingleThreadExecutor().submit(() -> {
            try {
                List<TransactionEntity> txns = transactionRepository.findAll();
                for (TransactionEntity txn : txns) {
                    emitter.send(txn);
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * Returns schema and metadata for all supported banking endpoints
     */
    @GetMapping("/endpoints-metadata")
    public ResponseEntity<List<Map<String, Object>>> getEndpointsMetadata() {
        List<Map<String, Object>> endpoints = List.of(
                endpointMeta("/accounts/get", "accounts", "Account Balances & Types", "POST",
                        "Fetches account balances, account IDs, names, types (depository, credit, loan), and masks."),
                endpointMeta("/auth/get", "auth", "Account & Routing Numbers (ACH/Wire)", "POST",
                        "Fetches ACH routing numbers and full account numbers for direct ACH transfers."),
                endpointMeta("/transactions/sync", "transactions-sync", "Incremental Transaction Stream", "POST",
                        "Fetches added, modified, and removed transactions with enrichment and categorization."),
                endpointMeta("/transactions/recurring/get", "recurring", "Recurring Inflow & Outflow Streams", "POST",
                        "Detects subscription charges, recurring bills, and payroll inflow streams."),
                endpointMeta("/identity/get", "identity", "KYC Identity & Owner Profiles", "POST",
                        "Fetches legal owner names, verified physical addresses, emails, and phone numbers."),
                endpointMeta("/item/get", "item", "Institution & Webhook Health Status", "POST",
                        "Provides metadata about the consented institution, update webhook, and token health."),
                endpointMeta("/liabilities/get", "liabilities", "Credit Cards, Student Loans & Mortgages", "POST",
                        "Extracts detailed liabilities data including APRs, minimum payments, and due dates."),
                endpointMeta("/investments/holdings/get", "holdings", "Investment Portfolio Holdings & Securities", "POST",
                        "Fetches stocks, ETFs, mutual funds, quantities, cost basis, and real-time market prices."),
                endpointMeta("/investments/transactions/get", "investments-transactions", "Investment Trading Activity", "POST",
                        "Fetches investment trade transactions, fees, dividends, and buying/selling history.")
        );
        return ResponseEntity.ok(endpoints);
    }

    /**
     * Retrieve the database records currently saved in the DB
     */
    @GetMapping({"/data", "/db-data"})
    public ResponseEntity<Map<String, Object>> getDatabaseData() {
        List<AccountEntity> accounts = accountRepository.findAllWithTransactions();
        List<TransactionEntity> recentTxns = transactionRepository.findAll();

        recentTxns.sort((a, b) -> Objects.toString(b.getTransactionTimestamp(), "")
                .compareTo(Objects.toString(a.getTransactionTimestamp(), "")));

        BigDecimal totalBal = accounts.stream()
                .map(a -> a.getCurrentBalance() != null ? a.getCurrentBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accountsCount", accounts.size());
        data.put("transactionsCount", recentTxns.size());
        data.put("totalBalance", totalBal);
        data.put("accounts", accounts);
        data.put("recentTransactions", recentTxns.stream().limit(100).toList());

        return ResponseEntity.ok(data);
    }

    private Map<String, Object> endpointMeta(String path, String slug, String title, String method, String description) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("path", path);
        m.put("slug", slug);
        m.put("title", title);
        m.put("method", method);
        m.put("description", description);
        return m;
    }

    private String mask(String str) {
        if (str == null || str.length() < 8) return "****";
        return str.substring(0, 4) + "..." + str.substring(str.length() - 4);
    }
}
