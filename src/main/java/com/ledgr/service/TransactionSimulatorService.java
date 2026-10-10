package com.ledgr.service;

import com.ledgr.entity.AccountEntity;
import com.ledgr.entity.TransactionEntity;
import com.ledgr.repository.AccountRepository;
import com.ledgr.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionSimulatorService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private ScheduledExecutorService streamingExecutor;
    private final CopyOnWriteArrayList<Consumer<TransactionEntity>> listeners = new CopyOnWriteArrayList<>();
    private final Random random = new Random();

    public record MockScenario(String narration, String payee, String category, String type, String mode, double minAmt, double maxAmt) {}

    private final List<MockScenario> scenarios = List.of(
            new MockScenario("UPI/428917263/swiggy@icici/Food Delivery", "Swiggy", "FOOD_DINING", "DEBIT", "UPI", 120.0, 480.0),
            new MockScenario("UPI/981273912/zomato@hdfcbank/Order #8372", "Zomato", "FOOD_DINING", "DEBIT", "UPI", 150.0, 650.0),
            new MockScenario("UPI/582910384/ola.money@axis/Cab commute", "Ola", "TRAVEL_COMMUTE", "DEBIT", "UPI", 45.0, 180.0),
            new MockScenario("UPI/772183921/uber.ride@kotak/Late night ride", "Uber", "TRAVEL_COMMUTE", "DEBIT", "UPI", 90.0, 320.0),
            new MockScenario("UPI/839102834/amzn.pay@sbi/Online Order", "Amazon", "SHOPPING_ECOMMERCE", "DEBIT", "UPI", 199.0, 1499.0),
            new MockScenario("UPI/192837465/simpl.pay@axis/Monthly Tab", "Simpl", "BNPL_LIFESTYLE", "DEBIT", "UPI", 450.0, 2200.0),
            new MockScenario("ACH/NETFLIX/48192839/Subscription", "Netflix", "SUBSCRIPTIONS", "DEBIT", "CARD", 199.0, 199.0),
            new MockScenario("UPI/991827364/spotify@hdfc/Music Subscription", "Spotify", "SUBSCRIPTIONS", "DEBIT", "UPI", 59.0, 59.0),
            new MockScenario("UPI/628192738/roommate.split@axis/Utility Share", "Roommate Split", "ESCROW_SPLIT", "CREDIT", "UPI", 500.0, 2500.0),
            new MockScenario("NEFT-UNIV928371-RESEARCH STIPEND", "University Stipend", "SCHOLARSHIP_STIPEND", "CREDIT", "FT", 8000.0, 15000.0),
            new MockScenario("UPI/382910283/freelance.client@icici/Freelance Payment", "Freelance Client", "FREELANCE_INCOME", "CREDIT", "UPI", 3500.0, 12000.0),
            new MockScenario("ATM/ICIC092/CAMPUS ATM CASH WITHDRAWAL", "ATM Cash", "ATM_CASH_WITHDRAWAL", "DEBIT", "ATM", 500.0, 2000.0)
    );

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
            throw new IllegalStateException("No accounts present in database. Run Banking Sync first.");
        }

        AccountEntity targetAccount = accounts.stream()
                .filter(a -> preferredAccountNumber != null ? a.getAccountNumber().equals(preferredAccountNumber) : ("SAVINGS".equalsIgnoreCase(a.getAccountType()) || "CHECKING".equalsIgnoreCase(a.getAccountType())))
                .findFirst()
                .orElse(accounts.get(0));

        MockScenario scenario = scenarios.get(random.nextInt(scenarios.size()));
        double rawAmt = scenario.minAmt() + (scenario.maxAmt() - scenario.minAmt()) * random.nextDouble();
        BigDecimal amount = BigDecimal.valueOf(rawAmt).setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentBal = targetAccount.getCurrentBalance() != null ? targetAccount.getCurrentBalance() : BigDecimal.ZERO;
        BigDecimal newBal;
        if ("CREDIT".equalsIgnoreCase(scenario.type())) {
            newBal = currentBal.add(amount);
        } else {
            newBal = currentBal.subtract(amount);
            if (newBal.compareTo(BigDecimal.ZERO) < 0) {
                amount = BigDecimal.valueOf(25.00);
                newBal = currentBal.subtract(amount);
            }
        }

        targetAccount.setCurrentBalance(newBal);
        accountRepository.save(targetAccount);

        String txnId = "TXN" + (10000000 + random.nextInt(89999999));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String valueDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        TransactionEntity txn = TransactionEntity.builder()
                .account(targetAccount)
                .txnId(txnId)
                .txnType(scenario.type())
                .paymentMode(scenario.mode())
                .amount(amount)
                .balanceAfterTxn(newBal)
                .transactionTimestamp(timestamp)
                .valueDate(valueDate)
                .narration(scenario.narration())
                .referenceNumber("RFN" + (100000000 + random.nextInt(899999999)))
                .category(scenario.category())
                .merchantPayee(scenario.payee())
                .build();

        TransactionEntity saved = transactionRepository.save(txn);
        log.info("Live Simulated Txn: {} | {} | {} | Bal: {}", txnId, scenario.payee(), amount, newBal);

        for (Consumer<TransactionEntity> listener : listeners) {
            try {
                listener.accept(saved);
            } catch (Exception e) {
                log.error("Error notifying transaction listener: {}", e.getMessage());
            }
        }

        return saved;
    }

    public synchronized void startLiveStreaming(int intervalSeconds) {
        if (streamingExecutor != null && !streamingExecutor.isShutdown()) {
            return;
        }
        streamingExecutor = Executors.newSingleThreadScheduledExecutor();
        streamingExecutor.scheduleAtFixedRate(() -> {
            try {
                generateAndPersistTransaction(null);
            } catch (Exception e) {
                log.warn("Error in live streaming transaction: {}", e.getMessage());
            }
        }, 2, intervalSeconds, TimeUnit.SECONDS);
        log.info("Started Live Transaction Stream (Interval: {}s)", intervalSeconds);
    }

    public synchronized void stopLiveStreaming() {
        if (streamingExecutor != null && !streamingExecutor.isShutdown()) {
            streamingExecutor.shutdownNow();
            streamingExecutor = null;
            log.info("Stopped Live Transaction Stream");
        }
    }

    public boolean isStreamingActive() {
        return streamingExecutor != null && !streamingExecutor.isShutdown();
    }
}
