package com.ledgr.sandbox.service;

import com.ledgr.entity.AccountEntity;
import com.ledgr.entity.TransactionEntity;
import com.ledgr.repository.AccountRepository;
import com.ledgr.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Service
public class RealtimeTransactionSimulatorService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeTransactionSimulatorService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private ScheduledExecutorService streamingExecutor;
    private int currentIntervalSeconds = 3;
    private volatile int minDelayMs = 1200;
    private volatile int maxDelayMs = 3200;
    private final java.util.concurrent.atomic.AtomicLong totalStreamedCount = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong lastDelayMs = new java.util.concurrent.atomic.AtomicLong(2000);
    private final CopyOnWriteArrayList<Consumer<TransactionEntity>> listeners = new CopyOnWriteArrayList<>();
    private final ConcurrentLinkedDeque<TransactionEntity> recentStreamedTransactions = new ConcurrentLinkedDeque<>();
    private final Random random = new Random();

    public record MockScenario(String narration, String payee, String category, String type, String mode, double minAmt, double maxAmt) {}

    private final List<MockScenario> scenarios = List.of(
            new MockScenario("UPI/428917263/swiggy@icici/Food Delivery/BTM Layout", "Swiggy", "FOOD_DINING", "DEBIT", "UPI", 120.0, 480.0),
            new MockScenario("UPI/981273912/zomato@hdfcbank/Order #8372", "Zomato", "FOOD_DINING", "DEBIT", "UPI", 150.0, 650.0),
            new MockScenario("UPI/582910384/ola.money@axis/Auto ride to College Campus", "Ola", "TRAVEL_COMMUTE", "DEBIT", "UPI", 45.0, 180.0),
            new MockScenario("UPI/772183921/uber.ride@kotak/Late night cab", "Uber", "TRAVEL_COMMUTE", "DEBIT", "UPI", 90.0, 320.0),
            new MockScenario("UPI/839102834/amzn.pay@sbi/Stationery & Electronics", "Amazon", "SHOPPING_ECOMMERCE", "DEBIT", "UPI", 199.0, 1499.0),
            new MockScenario("UPI/192837465/simpl.pay@axis/BNPL Monthly Grocery Tab", "Simpl", "BNPL_LIFESTYLE", "DEBIT", "UPI", 450.0, 2200.0),
            new MockScenario("ACH/NETFLIX/48192839/Monthly Student Subscription", "Netflix", "SUBSCRIPTIONS", "DEBIT", "CARD", 199.0, 199.0),
            new MockScenario("UPI/991827364/spotify.india@hdfc/Music Premium Student Plan", "Spotify", "SUBSCRIPTIONS", "DEBIT", "UPI", 59.0, 59.0),
            new MockScenario("UPI/628192738/roommate.split@okaxis/WiFi & Electricity Share", "Roommate Escrow", "ESCROW_SPLIT", "CREDIT", "UPI", 500.0, 2500.0),
            new MockScenario("NEFT-UNIV928371-RESEARCH STIPEND-DRDO LAB PROJECT", "University Stipend", "SCHOLARSHIP_STIPEND", "CREDIT", "FT", 8000.0, 15000.0),
            new MockScenario("UPI/382910283/freelance.client@icici/Frontend UI Design Project", "Freelance Client", "FREELANCE_INCOME", "CREDIT", "UPI", 3500.0, 12000.0),
            new MockScenario("ATM/ICIC092/CAMPUS ATM CASH WITHDRAWAL", "ATM Cash", "ATM_CASH_WITHDRAWAL", "DEBIT", "ATM", 500.0, 2000.0),
            new MockScenario("UPI/102938475/college.canteen@sbi/Lunch & Chai", "Campus Canteen", "FOOD_DINING", "DEBIT", "UPI", 35.0, 120.0),
            new MockScenario("UPI/558291049/blinkit@hdfc/Hostel Snacks & Energy Drinks", "Blinkit", "FOOD_DINING", "DEBIT", "UPI", 85.0, 420.0),
            new MockScenario("UPI/771928301/bookmyshow@icici/Weekend Movie Ticket", "BookMyShow", "ENTERTAINMENT", "DEBIT", "UPI", 220.0, 680.0),
            new MockScenario("UPI/882910394/campus.xerox@sbi/Exam Notes Printing", "Campus Stationery", "EDUCATION", "DEBIT", "UPI", 25.0, 150.0)
    );

    public RealtimeTransactionSimulatorService(AccountRepository accountRepository,
                                               TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public void registerListener(Consumer<TransactionEntity> listener) {
        listeners.add(listener);
    }

    public void unregisterListener(Consumer<TransactionEntity> listener) {
        listeners.remove(listener);
    }

    @Transactional
    public TransactionEntity generateAndPersistTransaction(String preferredAccountNumber) {
        List<AccountEntity> accounts = accountRepository.findAll();
        if (accounts.isEmpty()) {
            log.debug("No accounts found in database. Skipping generation cycle until accounts are seeded.");
            return null;
        }

        // Pick account in random order with preference for Savings/Deposit
        AccountEntity targetAccount;
        if (preferredAccountNumber != null) {
            targetAccount = accounts.stream()
                    .filter(a -> a.getAccountNumber().equals(preferredAccountNumber))
                    .findFirst()
                    .orElse(accounts.get(0));
        } else {
            List<AccountEntity> activeAccounts = accounts.stream()
                    .filter(a -> "SAVINGS".equalsIgnoreCase(a.getAccountType()) || "DEPOSIT".equalsIgnoreCase(a.getFiType()))
                    .toList();
            if (!activeAccounts.isEmpty()) {
                targetAccount = activeAccounts.get(random.nextInt(activeAccounts.size()));
            } else {
                targetAccount = accounts.get(random.nextInt(accounts.size()));
            }
        }

        String narration;
        String payee;
        String category;
        String txnType;
        String paymentMode;
        BigDecimal amount;

        // Repetition logic: 40% chance to repeat and adapt an existing historical transaction, 60% chance mock scenario
        List<TransactionEntity> existingTxns = transactionRepository.findByAccount_IdOrderByTransactionTimestampDesc(targetAccount.getId());
        if (existingTxns != null && !existingTxns.isEmpty() && random.nextDouble() < 0.40) {
            TransactionEntity sampleToRepeat = existingTxns.get(random.nextInt(existingTxns.size()));
            narration = sampleToRepeat.getNarration() != null ? sampleToRepeat.getNarration() : "Recurring Transaction";
            payee = sampleToRepeat.getMerchantPayee() != null ? sampleToRepeat.getMerchantPayee() : "Merchant Partner";
            category = sampleToRepeat.getCategory() != null ? sampleToRepeat.getCategory() : "GENERAL";
            txnType = sampleToRepeat.getTxnType() != null ? sampleToRepeat.getTxnType() : "DEBIT";
            paymentMode = sampleToRepeat.getPaymentMode() != null ? sampleToRepeat.getPaymentMode() : "UPI";
            
            // Variate amount slightly (+/- 10%) or use exact for repetition
            BigDecimal baseAmt = sampleToRepeat.getAmount() != null ? sampleToRepeat.getAmount() : BigDecimal.valueOf(100.0);
            double varianceFactor = 0.90 + (0.20 * random.nextDouble());
            amount = baseAmt.multiply(BigDecimal.valueOf(varianceFactor)).setScale(2, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.valueOf(5.0)) < 0) {
                amount = BigDecimal.valueOf(50.00);
            }
        } else {
            MockScenario scenario = scenarios.get(random.nextInt(scenarios.size()));
            narration = scenario.narration();
            payee = scenario.payee();
            category = scenario.category();
            txnType = scenario.type();
            paymentMode = scenario.mode();
            double rawAmt = scenario.minAmt() + (scenario.maxAmt() - scenario.minAmt()) * random.nextDouble();
            amount = BigDecimal.valueOf(rawAmt).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal currentBal = targetAccount.getCurrentBalance() != null ? targetAccount.getCurrentBalance() : BigDecimal.ZERO;
        BigDecimal newBal;
        if ("CREDIT".equalsIgnoreCase(txnType)) {
            newBal = currentBal.add(amount);
        } else {
            newBal = currentBal.subtract(amount);
            if (newBal.compareTo(BigDecimal.valueOf(500.00)) < 0) {
                // If balance drops low, simulate a parent transfer / stipend credit to keep liquidity flowing
                txnType = "CREDIT";
                category = "PARENT_ALLOWANCE";
                payee = "Parent UPI Transfer";
                narration = "UPI/998127492/family.support@sbi/Monthly Allowance Topup";
                amount = BigDecimal.valueOf(5000.00 + random.nextInt(5000));
                newBal = currentBal.add(amount);
            }
        }

        targetAccount.setCurrentBalance(newBal);
        accountRepository.save(targetAccount);

        String txnId = "TXN" + (10000000 + random.nextInt(89999999));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss+05:30"));
        String valueDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        TransactionEntity txn = TransactionEntity.builder()
                .account(targetAccount)
                .txnId(txnId)
                .txnType(txnType)
                .paymentMode(paymentMode)
                .amount(amount)
                .balanceAfterTxn(newBal)
                .transactionTimestamp(timestamp)
                .valueDate(valueDate)
                .narration(narration)
                .referenceNumber("RFN" + (100000000 + random.nextInt(899999999)))
                .category(category)
                .merchantPayee(payee)
                .build();

        TransactionEntity saved = transactionRepository.save(txn);
        totalStreamedCount.incrementAndGet();

        // Maintain in thread-safe circular buffer (keep last 50)
        recentStreamedTransactions.addFirst(saved);
        while (recentStreamedTransactions.size() > 50) {
            recentStreamedTransactions.pollLast();
        }

        log.info("⚡ Live Simulated Txn #{}: {} | {} | {} ₹{} | Bal: ₹{}",
                totalStreamedCount.get(), txnId, payee, txnType, amount, newBal);

        // Notify all registered listeners (SSE, WebSockets, background tasks)
        for (Consumer<TransactionEntity> listener : listeners) {
            try {
                listener.accept(saved);
            } catch (Exception e) {
                log.error("Error notifying transaction listener: {}", e.getMessage());
            }
        }

        return saved;
    }

    /**
     * Start live streaming with random delay and infinite recurrence so data flow never stops
     */
    public synchronized void startLiveStreamingWithRandomDelay(int minMs, int maxMs) {
        if (streamingExecutor != null && !streamingExecutor.isShutdown()) {
            return;
        }
        this.minDelayMs = Math.max(minMs, 500);
        this.maxDelayMs = Math.max(maxMs, this.minDelayMs + 200);
        streamingExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "transaction-simulator-stream");
            t.setDaemon(true);
            return t;
        });

        log.info("🚀 Started Live Real-Time Transaction Stream with RANDOM DELAYS ({}ms - {}ms) & Repetition",
                this.minDelayMs, this.maxDelayMs);

        scheduleNextRandomCycle();
    }

    private void scheduleNextRandomCycle() {
        if (streamingExecutor == null || streamingExecutor.isShutdown()) {
            return;
        }
        long delay = minDelayMs + (long) (random.nextDouble() * (maxDelayMs - minDelayMs));
        lastDelayMs.set(delay);

        try {
            streamingExecutor.schedule(() -> {
                try {
                    generateAndPersistTransaction(null);
                } catch (Exception e) {
                    log.warn("Non-fatal stream generation notice (data flow will continue): {}", e.getMessage());
                } finally {
                    scheduleNextRandomCycle();
                }
            }, delay, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException ignored) {
            // Executor was shutdown
        }
    }

    public synchronized void startLiveStreaming(int intervalSeconds) {
        int intervalMs = Math.max(intervalSeconds, 1) * 1000;
        startLiveStreamingWithRandomDelay(Math.max(intervalMs - 500, 500), intervalMs + 1000);
    }

    @jakarta.annotation.PreDestroy
    public synchronized void stopLiveStreaming() {
        if (streamingExecutor != null && !streamingExecutor.isShutdown()) {
            streamingExecutor.shutdownNow();
            streamingExecutor = null;
            log.info("⏹️ Stopped Live Real-Time Transaction Stream");
        }
    }

    public boolean isStreamingActive() {
        return streamingExecutor != null && !streamingExecutor.isShutdown();
    }

    public int getCurrentIntervalSeconds() {
        return (int) (lastDelayMs.get() / 1000);
    }

    public long getLastDelayMs() {
        return lastDelayMs.get();
    }

    public int getMinDelayMs() {
        return minDelayMs;
    }

    public int getMaxDelayMs() {
        return maxDelayMs;
    }

    public long getTotalStreamedCount() {
        return totalStreamedCount.get();
    }

    public List<TransactionEntity> getRecentStreamTransactions(int limit) {
        int count = Math.max(limit, 1);
        return recentStreamedTransactions.stream().limit(count).toList();
    }
}
