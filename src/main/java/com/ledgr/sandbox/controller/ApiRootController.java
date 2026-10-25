package com.ledgr.sandbox.controller;

import com.ledgr.sandbox.repository.AccountRepository;
import com.ledgr.sandbox.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@CrossOrigin(origins = "*")
public class ApiRootController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public ApiRootController(AccountRepository accountRepository,
                             TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping(value = {"/api", "/api/v1"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApiOverview() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("service", "Ledgr RBI Account Aggregator (AA) & Student FinTech Backend");
        root.put("version", "1.0.0");
        root.put("status", "ONLINE");
        root.put("architecture", "RESTful Web Application");
        root.put("timestamp", LocalDateTime.now().toString());

        Map<String, Object> endpoints = new LinkedHashMap<>();

        // RBI AA Protocol
        endpoints.put("POST /api/v1/aa/consents", "Create ReBIT consent request in Setu AA Sandbox");
        endpoints.put("GET /api/v1/aa/consents/{consentId}", "Fetch status of consent");
        endpoints.put("POST /api/v1/aa/consents/{consentId}/approve", "Approve consent with OTP (default: 123456)");
        endpoints.put("POST /api/v1/aa/consents/{consentId}/revoke", "Revoke active consent");
        endpoints.put("POST /api/v1/aa/fi/request", "Initiate financial information session");
        endpoints.put("GET /api/v1/aa/fi/fetch/{sessionId}", "Fetch encrypted statement payload");
        endpoints.put("GET /api/v1/aa/mock/raw", "Get raw ReBIT multi-account mock dataset");

        // FIU Data Ingestion & Accounts
        endpoints.put("POST /api/v1/fiu/sync", "One-click AA fetch and ingestion into database");
        endpoints.put("GET /api/v1/fiu/accounts", "List all linked bank accounts (Savings, FD, RD)");
        endpoints.put("GET /api/v1/fiu/accounts/{accountId}", "Get single bank account details");
        endpoints.put("GET /api/v1/fiu/accounts/{accountId}/transactions", "Get transactions for specific account");
        endpoints.put("GET /api/v1/fiu/transactions", "Cross-account transactions with search, category & type filters");
        endpoints.put("GET /api/v1/fiu/analytics/overview", "Student financial overview (net worth, burn rate, runway, category breakdown)");

        // Real-Time Simulator
        endpoints.put("POST /api/v1/simulator/transactions/generate", "Trigger an instant simulated transaction");
        endpoints.put("POST /api/v1/simulator/stream/start?intervalSeconds=3", "Start background transaction streaming");
        endpoints.put("POST /api/v1/simulator/stream/stop", "Stop background transaction stream");
        endpoints.put("GET /api/v1/simulator/stream/status", "Check if live stream is running");
        endpoints.put("GET /api/v1/simulator/stream/events", "Server-Sent Events (SSE) live stream feed");

        // FinTech Tools
        endpoints.put("GET /api/v1/tools/runway", "Scholarship runway calculator (custom burn & FD buffer options)");
        endpoints.put("GET /api/v1/tools/salary-sandbox", "Post-graduation CTC-to-in-hand salary simulator");
        endpoints.put("GET /api/v1/tools/roommate-splits", "Roommate expense splitter and settlement tracking");

        // Health
        endpoints.put("GET /api/v1/health", "System health check and live database metrics");

        root.put("endpoints", endpoints);
        return ResponseEntity.ok(root);
    }

    @GetMapping("/api/v1/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        long accounts = accountRepository.count();
        long txns = transactionRepository.count();

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("serverTime", LocalDateTime.now().toString());
        health.put("databaseConnected", true);
        health.put("accountsCount", accounts);
        health.put("transactionsCount", txns);
        health.put("message", accounts == 0 ? "Database is empty. Call POST /api/v1/fiu/sync to ingest AA data." : "System operational with " + accounts + " accounts and " + txns + " transactions.");

        return ResponseEntity.ok(health);
    }
}
