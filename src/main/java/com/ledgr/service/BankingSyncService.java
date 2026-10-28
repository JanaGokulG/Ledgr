package com.ledgr.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgr.config.BankingProperties;
import com.ledgr.entity.AccountEntity;
import com.ledgr.entity.TransactionEntity;
import com.ledgr.repository.AccountRepository;
import com.ledgr.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankingSyncService {

    private final BankingProperties bankingProperties;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    // Cache active access token for quick repeated endpoint inspections
    private final Map<String, String> tokenCache = new ConcurrentHashMap<>();

    /**
     * Executes an HTTP POST request to Banking Sandbox API
     */
    public Map<String, Object> callBankingApi(String endpoint, Map<String, Object> payload) {
        String url = bankingProperties.getBaseUrl() + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
        try {
            String requestBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(35))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            long start = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long duration = System.currentTimeMillis() - start;

            Map<String, Object> resultMap = objectMapper.readValue(
                    response.body(), new TypeReference<Map<String, Object>>() {}
            );

            resultMap.put("_meta_http_status", response.statusCode());
            resultMap.put("_meta_duration_ms", duration);
            resultMap.put("_meta_endpoint", endpoint);

            if (response.statusCode() >= 400) {
                log.warn("Banking API [{}] returned HTTP {}: {}", endpoint, response.statusCode(), response.body());
            } else {
                log.info("Banking API [{}] completed successfully in {} ms (HTTP {})", endpoint, duration, response.statusCode());
            }

            return resultMap;
        } catch (Exception e) {
            log.error("Error executing Banking API call to [{}]: {}", url, e.getMessage(), e);
            Map<String, Object> errMap = new LinkedHashMap<>();
            errMap.put("error_code", "CLIENT_HTTP_EXCEPTION");
            errMap.put("error_message", e.getMessage());
            errMap.put("_meta_http_status", 500);
            errMap.put("_meta_endpoint", endpoint);
            return errMap;
        }
    }

    /**
     * Obtains or generates a fresh banking sandbox access token
     */
    public synchronized String getOrGenerateAccessToken(boolean forceFresh) {
        if (!forceFresh && tokenCache.containsKey("active_token")) {
            return tokenCache.get("active_token");
        }

        Map<String, Object> tokenReq = new LinkedHashMap<>();
        tokenReq.put("client_id", bankingProperties.getClientId());
        tokenReq.put("secret", bankingProperties.getSecret());
        tokenReq.put("institution_id", bankingProperties.getInstitutionId());
        tokenReq.put("initial_products", bankingProperties.getInitialProducts());
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("webhook", bankingProperties.getWebhookUrl());
        options.put("override_username", bankingProperties.getOverrideUsername());
        options.put("override_password", bankingProperties.getOverridePassword());
        tokenReq.put("options", options);

        Map<String, Object> createRes = callBankingApi("/sandbox/public_token/create", tokenReq);
        String publicToken = (String) createRes.get("public_token");
        if (publicToken == null) {
            throw new IllegalStateException("Failed to generate banking public token: " + createRes);
        }

        Map<String, Object> exchReq = new LinkedHashMap<>();
        exchReq.put("client_id", bankingProperties.getClientId());
        exchReq.put("secret", bankingProperties.getSecret());
        exchReq.put("public_token", publicToken);

        Map<String, Object> exchRes = callBankingApi("/item/public_token/exchange", exchReq);
        String accessToken = (String) exchRes.get("access_token");
        if (accessToken == null) {
            throw new IllegalStateException("Failed to exchange banking public token: " + exchRes);
        }

        tokenCache.put("active_token", accessToken);
        log.info("Successfully established Banking Item & Access Token: {}", accessToken);
        return accessToken;
    }

    private Map<String, Object> createBaseAuthPayload(String accessToken) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("client_id", bankingProperties.getClientId());
        payload.put("secret", bankingProperties.getSecret());
        payload.put("access_token", accessToken != null ? accessToken : getOrGenerateAccessToken(false));
        return payload;
    }

    // 1. /accounts/get
    public Map<String, Object> getAccounts(String accessToken) {
        return callBankingApi("/accounts/get", createBaseAuthPayload(accessToken));
    }

    // 2. /auth/get
    public Map<String, Object> getAuth(String accessToken) {
        return callBankingApi("/auth/get", createBaseAuthPayload(accessToken));
    }

    // 3. /transactions/sync
    public Map<String, Object> syncTransactions(String accessToken, String cursor) {
        Map<String, Object> payload = createBaseAuthPayload(accessToken);
        if (cursor != null && !cursor.isBlank()) {
            payload.put("cursor", cursor);
        }
        return callBankingApi("/transactions/sync", payload);
    }

    /**
     * Fetches transaction details from API and inserts each into DB with inter-transaction delay
     */
    @Transactional
    public Map<String, Object> syncAndPersistTransactions(String accessToken, String cursor) {
        String token = (accessToken != null && !accessToken.isBlank()) ? accessToken : getOrGenerateAccessToken(false);
        Map<String, Object> apiRes = syncTransactions(token, cursor);
        List<Map<String, Object>> added = castList(apiRes.get("added"));
        List<Map<String, Object>> modified = castList(apiRes.get("modified"));

        List<Map<String, Object>> allTxns = new ArrayList<>();
        if (added != null) allTxns.addAll(added);
        if (modified != null) allTxns.addAll(modified);

        int inserted = 0;
        List<AccountEntity> accounts = accountRepository.findAll();
        AccountEntity defaultAccount = accounts.isEmpty() ? null : accounts.get(0);

        for (Map<String, Object> rawTxn : allTxns) {
            String txnId = (String) rawTxn.get("transaction_id");
            simulateTransactionDelay(txnId);

            String accId = (String) rawTxn.get("account_id");
            AccountEntity targetAccount = defaultAccount;
            if (accId != null && !accounts.isEmpty()) {
                targetAccount = accounts.stream()
                        .filter(a -> a.getAccountNumber().contains(accId.substring(0, Math.min(6, accId.length()))))
                        .findFirst()
                        .orElse(defaultAccount);
            }

            persistTransaction(rawTxn, targetAccount, BigDecimal.ZERO);
            inserted++;
        }

        apiRes.put("transactionsInsertedIntoDb", inserted);
        apiRes.put("dbStatus", "INSERTED");
        log.info("Successfully fetched and inserted {} transactions into database.", inserted);
        return apiRes;
    }

    // 4. /transactions/recurring/get
    public Map<String, Object> getRecurringTransactions(String accessToken) {
        return callBankingApi("/transactions/recurring/get", createBaseAuthPayload(accessToken));
    }

    // 5. /identity/get
    public Map<String, Object> getIdentity(String accessToken) {
        return callBankingApi("/identity/get", createBaseAuthPayload(accessToken));
    }

    // 6. /item/get
    public Map<String, Object> getItem(String accessToken) {
        return callBankingApi("/item/get", createBaseAuthPayload(accessToken));
    }

    // 7. /liabilities/get
    public Map<String, Object> getLiabilities(String accessToken) {
        return callBankingApi("/liabilities/get", createBaseAuthPayload(accessToken));
    }

    // 8. /investments/holdings/get
    public Map<String, Object> getInvestmentHoldings(String accessToken) {
        return callBankingApi("/investments/holdings/get", createBaseAuthPayload(accessToken));
    }

    // 9. /investments/transactions/get
    public Map<String, Object> getInvestmentTransactions(String accessToken, String startDate, String endDate) {
        Map<String, Object> payload = createBaseAuthPayload(accessToken);
        payload.put("start_date", startDate != null ? startDate : LocalDate.now().minusMonths(6).toString());
        payload.put("end_date", endDate != null ? endDate : LocalDate.now().toString());
        return callBankingApi("/investments/transactions/get", payload);
    }

    /**
     * Simulates delay specifically between two transactions being fetched / ingested
     */
    public void simulateTransactionDelay(String txnId) {
        long delay = bankingProperties.getTransactionDelayMs();
        if (delay > 0) {
            log.info("⏳ Simulating delay ({} ms) before fetching/processing transaction [{}]", delay, txnId);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Transaction delay interrupted for: {}", txnId);
            }
        }
    }

    /**
     * Dispatcher for any of the 9 banking endpoints
     */
    public Map<String, Object> dispatchEndpoint(String endpoint, Map<String, Object> customParams) {
        String token = (customParams != null && customParams.containsKey("access_token"))
                ? (String) customParams.get("access_token")
                : getOrGenerateAccessToken(false);

        String normalized = endpoint.toLowerCase().trim();
        if (normalized.endsWith("/accounts/get") || normalized.equals("accounts")) {
            return getAccounts(token);
        } else if (normalized.endsWith("/auth/get") || normalized.equals("auth")) {
            return getAuth(token);
        } else if (normalized.endsWith("/transactions/sync") || normalized.equals("transactions-sync") || normalized.equals("transactions")) {
            String cursor = customParams != null ? (String) customParams.get("cursor") : null;
            return syncAndPersistTransactions(token, cursor);
        } else if (normalized.endsWith("/transactions/recurring/get") || normalized.equals("recurring")) {
            return getRecurringTransactions(token);
        } else if (normalized.endsWith("/identity/get") || normalized.equals("identity")) {
            return getIdentity(token);
        } else if (normalized.endsWith("/item/get") || normalized.equals("item")) {
            return getItem(token);
        } else if (normalized.endsWith("/liabilities/get") || normalized.equals("liabilities")) {
            return getLiabilities(token);
        } else if (normalized.endsWith("/investments/holdings/get") || normalized.equals("investments-holdings") || normalized.equals("holdings")) {
            return getInvestmentHoldings(token);
        } else if (normalized.endsWith("/investments/transactions/get") || normalized.equals("investments-transactions")) {
            String start = customParams != null ? (String) customParams.get("start_date") : null;
            String end = customParams != null ? (String) customParams.get("end_date") : null;
            return getInvestmentTransactions(token, start, end);
        }

        // Fallback generic call
        Map<String, Object> payload = createBaseAuthPayload(token);
        if (customParams != null) {
            payload.putAll(customParams);
        }
        return callBankingApi(endpoint, payload);
    }

    /**
     * MASTER PIPELINE: Fetches mock data from banking API endpoints and persists it into the database.
     * Replaces previous static records with live data fetched directly from the banking API.
     */
    @Transactional
    public Map<String, Object> fetchAndIngestBankingData() {
        long startTime = System.currentTimeMillis();
        log.info("Starting live banking API data fetch & database ingestion pipeline...");

        List<Map<String, Object>> stepsCompleted = new ArrayList<>();

        // Step 1: Ensure active token (direct fetch without artificial pause)
        String accessToken = getOrGenerateAccessToken(true);
        stepsCompleted.add(Map.of("step", "AUTHENTICATION", "description", "Token established with Banking API", "time", LocalDateTime.now().toString()));

        // Step 2: Fetch Accounts
        Map<String, Object> accountsRes = getAccounts(accessToken);
        List<Map<String, Object>> rawAccounts = castList(accountsRes.get("accounts"));
        stepsCompleted.add(Map.of("step", "ACCOUNTS", "count", rawAccounts.size(), "time", LocalDateTime.now().toString()));

        // Step 3: Fetch Auth (Routing & Account numbers)
        Map<String, Object> authRes = getAuth(accessToken);
        Map<String, String> accountNumbersMap = extractAccountNumbers(authRes);
        Map<String, String> routingMap = extractRoutingNumbers(authRes);
        stepsCompleted.add(Map.of("step", "AUTH", "extractedAccounts", accountNumbersMap.size(), "time", LocalDateTime.now().toString()));

        // Step 4: Fetch Identity (Account Owner Info)
        Map<String, Object> identityRes = getIdentity(accessToken);
        Map<String, Map<String, String>> ownerProfileMap = extractOwnerProfiles(identityRes);
        stepsCompleted.add(Map.of("step", "IDENTITY", "extractedOwners", ownerProfileMap.size(), "time", LocalDateTime.now().toString()));

        // Step 5: Fetch Transactions
        Map<String, Object> txnsRes = syncTransactions(accessToken, null);
        List<Map<String, Object>> rawTransactions = castList(txnsRes.get("added"));
        stepsCompleted.add(Map.of("step", "TRANSACTIONS_SYNC", "count", rawTransactions.size(), "time", LocalDateTime.now().toString()));

        // Step 6: Detail Persistence
        log.info("Banking API returned {} accounts and {} transactions. Inserting into database with inter-transaction delay...",
                rawAccounts.size(), rawTransactions.size());

        // Group transactions by account_id
        Map<String, List<Map<String, Object>>> txnsByAccount = new HashMap<>();
        for (Map<String, Object> rawTxn : rawTransactions) {
            String accId = (String) rawTxn.get("account_id");
            if (accId != null) {
                txnsByAccount.computeIfAbsent(accId, k -> new ArrayList<>()).add(rawTxn);
            }
        }

        int accountsPersisted = 0;
        int transactionsPersisted = 0;
        BigDecimal totalBalanceSum = BigDecimal.ZERO;

        for (Map<String, Object> rawAcc : rawAccounts) {
            String rawAccountId = (String) rawAcc.get("account_id");
            String mask = (String) rawAcc.get("mask");
            String name = (String) rawAcc.get("name");
            String type = (String) rawAcc.get("type");
            String subtype = (String) rawAcc.get("subtype");

            // Derive account number
            String fullAccNum = accountNumbersMap.getOrDefault(rawAccountId,
                    mask != null ? "ACC-" + mask + "-" + rawAccountId.substring(0, Math.min(6, rawAccountId.length()))
                                 : "ACC-" + rawAccountId);

            Map<String, Object> balances = castMap(rawAcc.get("balances"));
            BigDecimal currentBal = BigDecimal.ZERO;
            String currency = "USD";
            if (balances != null) {
                Object curObj = balances.get("current");
                if (curObj != null) {
                    currentBal = new BigDecimal(curObj.toString());
                }
                if (balances.get("iso_currency_code") != null) {
                    currency = balances.get("iso_currency_code").toString();
                }
            }
            totalBalanceSum = totalBalanceSum.add(currentBal);

            // Fetch owner profile
            Map<String, String> owner = ownerProfileMap.getOrDefault(rawAccountId, defaultOwner());
            String holderName = owner.getOrDefault("name", "Verified Account Holder");
            String holderEmail = owner.getOrDefault("email", "user@financial-core.com");
            String holderPhone = owner.getOrDefault("phone", "555-0199");
            String holderAddress = owner.getOrDefault("address", "100 Financial Way, New York, NY");

            // Look up or create AccountEntity
            AccountEntity account = accountRepository.findByAccountNumber(fullAccNum)
                    .orElseGet(() -> AccountEntity.builder().accountNumber(fullAccNum).build());

            account.setFiType(type != null ? type.toUpperCase() : "DEPOSITORY");
            account.setAccountType(subtype != null ? subtype.toUpperCase() : "CHECKING");
            account.setHolderName(holderName + " (" + name + ")");
            account.setHolderPan("PAN" + (mask != null ? mask : "0000") + "USA");
            account.setHolderMobile(holderPhone);
            account.setHolderEmail(holderEmail);
            account.setHolderAddress(holderAddress);
            account.setCurrentBalance(currentBal);
            account.setCurrency(currency);
            account.setIfsc(routingMap.getOrDefault(rawAccountId, "FED0001001"));
            account.setBranchName("Commercial Bank (" + (type != null ? type : "General") + ")");
            account.setStatus("ACTIVE");
            account.setCkycCompliant(true);
            account.setNomineeStatus("VERIFIED");
            account.setLastSyncedAt(LocalDateTime.now());

            account.getTransactions().clear();
            AccountEntity savedAccount = accountRepository.save(account);
            accountsPersisted++;

            List<Map<String, Object>> accTxns = txnsByAccount.getOrDefault(rawAccountId, Collections.emptyList());
            for (Map<String, Object> rawTxn : accTxns) {
                String txnId = (String) rawTxn.get("transaction_id");
                simulateTransactionDelay(txnId);

                // Directly insert fetched transaction into DB
                persistTransaction(rawTxn, savedAccount, currentBal);
                transactionsPersisted++;
            }

            accountRepository.save(savedAccount);
        }

        long totalDuration = System.currentTimeMillis() - startTime;
        log.info("Banking API fetch & database insertion complete: {} accounts, {} transactions in {} ms.",
                accountsPersisted, transactionsPersisted, totalDuration);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "SUCCESS");
        result.put("source", "BANKING_API");
        result.put("environment", bankingProperties.getEnv());
        result.put("institutionId", bankingProperties.getInstitutionId());
        result.put("accountsPersisted", accountsPersisted);
        result.put("transactionsPersisted", transactionsPersisted);
        result.put("totalBalanceSum", totalBalanceSum);
        result.put("transactionDelayMs", bankingProperties.getTransactionDelayMs());
        result.put("durationMs", totalDuration);
        result.put("syncedAt", LocalDateTime.now().toString());
        result.put("stepsCompleted", stepsCompleted);

        List<Map<String, Object>> summaryList = new ArrayList<>();
        for (AccountEntity acc : accountRepository.findAllWithTransactions()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", acc.getId());
            item.put("accountNumber", acc.getAccountNumber());
            item.put("holderName", acc.getHolderName());
            item.put("type", acc.getAccountType());
            item.put("balance", acc.getCurrentBalance());
            item.put("currency", acc.getCurrency());
            item.put("transactionCount", acc.getTransactions() != null ? acc.getTransactions().size() : 0);
            summaryList.add(item);
        }
        result.put("accountsSummary", summaryList);

        return result;
    }

    private Map<String, String> extractAccountNumbers(Map<String, Object> authRes) {
        Map<String, String> map = new HashMap<>();
        Map<String, Object> numbers = castMap(authRes.get("numbers"));
        if (numbers == null) return map;

        List<Map<String, Object>> achList = castList(numbers.get("ach"));
        if (achList != null) {
            for (Map<String, Object> ach : achList) {
                String accId = (String) ach.get("account_id");
                String accNum = (String) ach.get("account");
                if (accId != null && accNum != null) {
                    map.put(accId, accNum);
                }
            }
        }
        return map;
    }

    private Map<String, String> extractRoutingNumbers(Map<String, Object> authRes) {
        Map<String, String> map = new HashMap<>();
        Map<String, Object> numbers = castMap(authRes.get("numbers"));
        if (numbers == null) return map;

        List<Map<String, Object>> achList = castList(numbers.get("ach"));
        if (achList != null) {
            for (Map<String, Object> ach : achList) {
                String accId = (String) ach.get("account_id");
                String routing = (String) ach.get("routing");
                if (accId != null && routing != null) {
                    map.put(accId, routing);
                }
            }
        }
        return map;
    }

    private Map<String, Map<String, String>> extractOwnerProfiles(Map<String, Object> identityRes) {
        Map<String, Map<String, String>> map = new HashMap<>();
        List<Map<String, Object>> accs = castList(identityRes.get("accounts"));
        if (accs == null) return map;

        for (Map<String, Object> acc : accs) {
            String accId = (String) acc.get("account_id");
            List<Map<String, Object>> owners = castList(acc.get("owners"));
            if (accId != null && owners != null && !owners.isEmpty()) {
                Map<String, Object> primaryOwner = owners.get(0);
                Map<String, String> profile = new HashMap<>();

                List<String> names = castStringList(primaryOwner.get("names"));
                profile.put("name", (names != null && !names.isEmpty()) ? names.get(0) : "Verified User");

                List<Map<String, Object>> emails = castList(primaryOwner.get("emails"));
                if (emails != null && !emails.isEmpty()) {
                    profile.put("email", Objects.toString(emails.get(0).get("data"), "user@financial-core.com"));
                }

                List<Map<String, Object>> phones = castList(primaryOwner.get("phone_numbers"));
                if (phones != null && !phones.isEmpty()) {
                    profile.put("phone", Objects.toString(phones.get(0).get("data"), "555-0199"));
                }

                List<Map<String, Object>> addresses = castList(primaryOwner.get("addresses"));
                if (addresses != null && !addresses.isEmpty()) {
                    Map<String, Object> addrData = castMap(addresses.get(0).get("data"));
                    if (addrData != null) {
                        String street = Objects.toString(addrData.get("street"), "");
                        String city = Objects.toString(addrData.get("city"), "");
                        String region = Objects.toString(addrData.get("region"), "");
                        profile.put("address", (street + ", " + city + ", " + region).trim());
                    }
                }
                map.put(accId, profile);
            }
        }
        return map;
    }

    private Map<String, String> defaultOwner() {
        return Map.of(
                "name", "Verified User",
                "email", "user@financial-core.com",
                "phone", "555-0199",
                "address", "100 Financial Way, New York, NY"
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> castList(Object obj) {
        if (obj instanceof List<?>) {
            return (List<T>) obj;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static List<String> castStringList(Object obj) {
        if (obj instanceof List<?>) {
            return (List<String>) obj;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object obj) {
        if (obj instanceof Map<?, ?>) {
            return (Map<String, Object>) obj;
        }
        return Collections.emptyMap();
    }

    /**
     * Normalizes and inserts a single fetched transaction directly into the database
     */
    @Transactional
    public TransactionEntity persistTransaction(Map<String, Object> rawTxn, AccountEntity account, BigDecimal fallbackBalance) {
        String txnId = (String) rawTxn.get("transaction_id");
        if (txnId == null) {
            txnId = UUID.randomUUID().toString();
        }

        Object amtObj = rawTxn.get("amount");
        BigDecimal amt = amtObj != null ? new BigDecimal(amtObj.toString()) : BigDecimal.ZERO;

        String txnType = "DEBIT";
        BigDecimal positiveAmount = amt;
        if (amt.compareTo(BigDecimal.ZERO) < 0) {
            txnType = "CREDIT";
            positiveAmount = amt.abs();
        }

        String date = (String) rawTxn.get("date");
        String narration = (String) rawTxn.get("name");
        String paymentChannel = (String) rawTxn.get("payment_channel");
        String merchantName = (String) rawTxn.get("merchant_name");

        String category = "LIFESTYLE";
        Map<String, Object> pfc = castMap(rawTxn.get("personal_finance_category"));
        if (pfc != null && pfc.get("primary") != null) {
            category = pfc.get("primary").toString();
        } else if (rawTxn.get("category") instanceof List<?> catList && !catList.isEmpty()) {
            category = catList.get(0).toString();
        }

        String finalTxnId = txnId;
        TransactionEntity txnEntity = transactionRepository.findByTxnId(finalTxnId)
                .orElseGet(() -> TransactionEntity.builder().txnId(finalTxnId).build());

        txnEntity.setAccount(account);
        txnEntity.setTxnType(txnType);
        txnEntity.setPaymentMode(paymentChannel != null ? paymentChannel.toUpperCase().replace(" ", "_") : "ONLINE");
        txnEntity.setAmount(positiveAmount);
        txnEntity.setBalanceAfterTxn(account != null && account.getCurrentBalance() != null ? account.getCurrentBalance() : fallbackBalance);
        txnEntity.setTransactionTimestamp(date != null ? date + "T12:00:00" : LocalDateTime.now().toString());
        txnEntity.setValueDate(date);
        txnEntity.setNarration(narration);
        txnEntity.setReferenceNumber(txnId);
        txnEntity.setCategory(category);
        txnEntity.setMerchantPayee(merchantName != null ? merchantName : (narration != null ? narration : "Verified Merchant"));

        TransactionEntity saved = transactionRepository.save(txnEntity);
        if (account != null && !account.getTransactions().contains(saved)) {
            account.getTransactions().add(saved);
        }
        log.info("💾 Persisted transaction to DB: [{}] - {} | {} {}", finalTxnId, merchantName, txnType, positiveAmount);
        return saved;
    }
}
