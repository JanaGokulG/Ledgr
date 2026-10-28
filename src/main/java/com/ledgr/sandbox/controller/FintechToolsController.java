package com.ledgr.sandbox.controller;

import com.ledgr.entity.AccountEntity;
import com.ledgr.entity.TransactionEntity;
import com.ledgr.repository.AccountRepository;
import com.ledgr.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/v1/tools")
@CrossOrigin(origins = "*")
public class FintechToolsController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public FintechToolsController(AccountRepository accountRepository,
                                  TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Scholarship Runway Calculator
     * Analyzes liquid savings & FD buffers against burn rate to project runway months.
     */
    @GetMapping("/runway")
    public ResponseEntity<Map<String, Object>> calculateRunway(
            @RequestParam(required = false) BigDecimal customMonthlyBurn,
            @RequestParam(required = false, defaultValue = "false") boolean includeFdBuffer) {

        List<AccountEntity> accounts = accountRepository.findAll();
        BigDecimal liquidSavings = BigDecimal.ZERO;
        BigDecimal fdAssets = BigDecimal.ZERO;

        for (AccountEntity acc : accounts) {
            BigDecimal bal = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : BigDecimal.ZERO;
            if ("SAVINGS".equalsIgnoreCase(acc.getAccountType()) || "DEPOSIT".equalsIgnoreCase(acc.getFiType())) {
                liquidSavings = liquidSavings.add(bal);
            } else if ("FIXED".equalsIgnoreCase(acc.getAccountType()) || "TERM-DEPOSIT".equalsIgnoreCase(acc.getFiType())) {
                fdAssets = fdAssets.add(acc.getMaturityAmount() != null ? acc.getMaturityAmount() : bal);
            }
        }

        BigDecimal monthlyBurn;
        if (customMonthlyBurn != null && customMonthlyBurn.compareTo(BigDecimal.ZERO) > 0) {
            monthlyBurn = customMonthlyBurn;
        } else {
            BigDecimal totalDebits = transactionRepository.getTotalDebits();
            if (totalDebits == null) totalDebits = BigDecimal.ZERO;
            monthlyBurn = totalDebits.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
            if (monthlyBurn.compareTo(BigDecimal.ZERO) == 0) {
                monthlyBurn = BigDecimal.valueOf(15000); // sensible default
            }
        }

        BigDecimal totalAvailable = includeFdBuffer ? liquidSavings.add(fdAssets) : liquidSavings;
        BigDecimal runwayMonths = totalAvailable.divide(monthlyBurn, 2, RoundingMode.HALF_UP);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("liquidSavings", liquidSavings);
        res.put("fdBufferAssets", fdAssets);
        res.put("totalCapitalAvailable", totalAvailable);
        res.put("includeFdBuffer", includeFdBuffer);
        res.put("monthlyBurnRate", monthlyBurn);
        res.put("runwayMonths", runwayMonths);
        res.put("safetyStatus", runwayMonths.compareTo(BigDecimal.valueOf(6)) >= 0 ? "COMFORTABLE" : (runwayMonths.compareTo(BigDecimal.valueOf(3)) >= 0 ? "MODERATE" : "CRITICAL"));

        return ResponseEntity.ok(res);
    }

    /**
     * Post-Graduation Salary Sandbox
     * CTC-to-in-hand salary simulator factoring in New Tax Regime, EPF, and city cost-of-living.
     */
    @GetMapping("/salary-sandbox")
    public ResponseEntity<Map<String, Object>> calculateSalarySandbox(
            @RequestParam(required = false, defaultValue = "1200000") BigDecimal ctc,
            @RequestParam(required = false, defaultValue = "Bangalore") String city) {

        // Basic salary usually 40% - 50% of CTC
        BigDecimal basicAnnual = ctc.multiply(BigDecimal.valueOf(0.40));
        BigDecimal epfAnnual = basicAnnual.multiply(BigDecimal.valueOf(0.12)); // 12% EPF
        BigDecimal standardDeduction = BigDecimal.valueOf(75000); // FY 2024-25 New Tax Regime

        // Taxable income under New Tax Regime
        BigDecimal taxableIncome = ctc.subtract(standardDeduction).subtract(epfAnnual);
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) taxableIncome = BigDecimal.ZERO;

        // New Tax Regime Slabs (up to 3L nil, 3-7L 5%, 7-10L 10%, 10-12L 15%, 12-15L 20%, above 15L 30%)
        // With 87A rebate for income up to 7L
        BigDecimal tax = BigDecimal.ZERO;
        if (taxableIncome.compareTo(BigDecimal.valueOf(700000)) > 0) {
            double income = taxableIncome.doubleValue();
            double t = 0;
            if (income > 300000) t += Math.min(income - 300000, 400000) * 0.05;
            if (income > 700000) t += Math.min(income - 700000, 300000) * 0.10;
            if (income > 1000000) t += Math.min(income - 1000000, 200000) * 0.15;
            if (income > 1200000) t += Math.min(income - 1200000, 300000) * 0.20;
            if (income > 1500000) t += (income - 1500000) * 0.30;
            // 4% Health & Education cess
            t = t * 1.04;
            tax = BigDecimal.valueOf(t).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal annualNetTakeHome = ctc.subtract(epfAnnual).subtract(tax);
        BigDecimal monthlyGross = ctc.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyInHand = annualNetTakeHome.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        // City cost of living index adjustments (estimated baseline living expenses)
        Map<String, Integer> cityLivingCosts = Map.of(
                "Bangalore", 35000,
                "Mumbai", 45000,
                "Delhi", 32000,
                "Hyderabad", 28000,
                "Pune", 27000
        );
        int estimatedLivingCost = cityLivingCosts.getOrDefault(city, 30000);
        BigDecimal monthlySavings = monthlyInHand.subtract(BigDecimal.valueOf(estimatedLivingCost));

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("annualCtc", ctc);
        res.put("monthlyGross", monthlyGross);
        res.put("annualEpfDeduction", epfAnnual);
        res.put("standardDeduction", standardDeduction);
        res.put("annualIncomeTax", tax);
        res.put("monthlyNetInHand", monthlyInHand);
        res.put("selectedCity", city);
        res.put("estimatedCityMonthlyLivingCost", estimatedLivingCost);
        res.put("projectedMonthlySavings", monthlySavings);

        return ResponseEntity.ok(res);
    }

    /**
     * Roommate Escrow & Expense Splitter
     * Aggregates shared dining, grocery, rent, utilities, and Wi-Fi transactions.
     */
    @GetMapping("/roommate-splits")
    public ResponseEntity<Map<String, Object>> getRoommateSplits(
            @RequestParam(required = false, defaultValue = "3") int totalRoommates) {

        int roommates = Math.max(totalRoommates, 1);
        List<TransactionEntity> transactions = transactionRepository.findAll();

        BigDecimal sharedTotal = BigDecimal.ZERO;
        List<Map<String, Object>> sharedTxns = new ArrayList<>();

        for (TransactionEntity t : transactions) {
            String cat = t.getCategory() != null ? t.getCategory() : "";
            String narr = t.getNarration() != null ? t.getNarration().toLowerCase() : "";
            if ("ESCROW_SPLIT".equalsIgnoreCase(cat) || narr.contains("roommate") || narr.contains("wifi") ||
                narr.contains("electricity") || narr.contains("rent") || narr.contains("grocery")) {
                sharedTotal = sharedTotal.add(t.getAmount());
                sharedTxns.add(Map.of(
                        "id", t.getId(),
                        "date", t.getTransactionTimestamp() != null ? t.getTransactionTimestamp() : "",
                        "narration", t.getNarration() != null ? t.getNarration() : "",
                        "amount", t.getAmount(),
                        "type", t.getTxnType()
                ));
            }
        }

        BigDecimal perPersonShare = sharedTotal.divide(BigDecimal.valueOf(roommates), 2, RoundingMode.HALF_UP);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("totalSharedExpenditure", sharedTotal);
        res.put("roommateCount", roommates);
        res.put("perPersonShare", perPersonShare);
        res.put("sharedTransactionsCount", sharedTxns.size());
        res.put("sharedTransactions", sharedTxns);

        return ResponseEntity.ok(res);
    }

    /**
     * Auto-Renewal Early Warnings & Minimum Balance Shield
     * Scans recurring auto-debit payments (Subscriptions, Recurring Deposits, EMIs, Utility Bills)
     * and alerts if balance is insufficient or will fall below minimum balance threshold.
     */
    @GetMapping("/renewal-warnings")
    public ResponseEntity<Map<String, Object>> getRenewalWarnings(
            @RequestParam(required = false, defaultValue = "1000") BigDecimal minBalanceThreshold,
            @RequestParam(required = false, defaultValue = "14") int lookaheadDays,
            @RequestParam(required = false) BigDecimal simulatedBalance) {

        List<AccountEntity> accounts = accountRepository.findAll();
        AccountEntity primaryAccount = null;
        BigDecimal actualLiquidBalance = BigDecimal.ZERO;

        for (AccountEntity acc : accounts) {
            BigDecimal bal = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : BigDecimal.ZERO;
            if ("SAVINGS".equalsIgnoreCase(acc.getAccountType()) || "DEPOSIT".equalsIgnoreCase(acc.getFiType())) {
                if (primaryAccount == null || bal.compareTo(actualLiquidBalance) > 0) {
                    primaryAccount = acc;
                    actualLiquidBalance = bal;
                }
            }
        }

        BigDecimal activeBalance = simulatedBalance != null ? simulatedBalance : actualLiquidBalance;
        String fundingAccountNumber = primaryAccount != null ? primaryAccount.getAccountNumber() : "SB-PRIMARY";

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> upcomingPayments = new ArrayList<>();
        BigDecimal totalUpcomingAmount = BigDecimal.ZERO;
        BigDecimal totalAtRiskAmount = BigDecimal.ZERO;
        int criticalCount = 0;
        int warningCount = 0;

        // 1. Scan Recurring Deposits (RD) from Accounts
        for (AccountEntity acc : accounts) {
            if ("RECURRING".equalsIgnoreCase(acc.getAccountType()) ||
                (acc.getRecurringAmount() != null && acc.getRecurringAmount().compareTo(BigDecimal.ZERO) > 0)) {
                
                BigDecimal amount = acc.getRecurringAmount() != null ? acc.getRecurringAmount() : BigDecimal.valueOf(1000);
                int dueDay = acc.getRecurringDepositDay() != null ? acc.getRecurringDepositDay() : 5;
                dueDay = Math.min(Math.max(dueDay, 1), 28);
                
                LocalDate dueDate = today.withDayOfMonth(dueDay);
                if (dueDate.isBefore(today)) {
                    dueDate = dueDate.plusMonths(1);
                }
                long daysRemaining = ChronoUnit.DAYS.between(today, dueDate);

                if (daysRemaining <= lookaheadDays) {
                    upcomingPayments.add(buildPaymentAlert(
                            "Recurring Deposit (RD) Installment",
                            "SAVINGS_RD",
                            amount,
                            dueDate.toString(),
                            (int) daysRemaining,
                            fundingAccountNumber,
                            activeBalance,
                            minBalanceThreshold,
                            "Mandatory bank RD installment deduction. Bounces trigger RD forfeiture and penalty charges."
                    ));
                    totalUpcomingAmount = totalUpcomingAmount.add(amount);
                }
            }
        }

        // 2. Scan Subscriptions & Recurring Bills from Transactions
        List<TransactionEntity> allTxns = transactionRepository.findAll();
        Map<String, TransactionEntity> latestRecurringMap = new LinkedHashMap<>();

        for (TransactionEntity t : allTxns) {
            String cat = t.getCategory() != null ? t.getCategory().toUpperCase() : "";
            String narr = t.getNarration() != null ? t.getNarration().toLowerCase() : "";
            String payee = t.getMerchantPayee() != null ? t.getMerchantPayee() : "Recurring Service";

            boolean isRecurring = cat.contains("SUBSCRIPTION") || cat.contains("ESCROW") ||
                    narr.contains("netflix") || narr.contains("spotify") || narr.contains("prime") ||
                    narr.contains("simpl") || narr.contains("wifi") || narr.contains("electricity") ||
                    narr.contains("hotstar") || narr.contains("gym") || narr.contains("chatgpt") || narr.contains("copilot");

            if (isRecurring && "DEBIT".equalsIgnoreCase(t.getTxnType())) {
                latestRecurringMap.put(payee.trim(), t);
            }
        }

        // Standard student recurring services list if history has items or default set
        if (latestRecurringMap.isEmpty()) {
            latestRecurringMap.put("Netflix Student Plan", TransactionEntity.builder()
                    .merchantPayee("Netflix")
                    .amount(BigDecimal.valueOf(199.00))
                    .category("SUBSCRIPTIONS")
                    .transactionTimestamp(today.minusDays(27).toString())
                    .narration("ACH/NETFLIX/48192839/Monthly Student Subscription")
                    .build());
            latestRecurringMap.put("Spotify Premium", TransactionEntity.builder()
                    .merchantPayee("Spotify")
                    .amount(BigDecimal.valueOf(59.00))
                    .category("SUBSCRIPTIONS")
                    .transactionTimestamp(today.minusDays(25).toString())
                    .narration("UPI/991827364/spotify.india@hdfc/Music Premium Student Plan")
                    .build());
            latestRecurringMap.put("Campus Wi-Fi & Maintenance", TransactionEntity.builder()
                    .merchantPayee("Campus Network")
                    .amount(BigDecimal.valueOf(450.00))
                    .category("UTILITIES")
                    .transactionTimestamp(today.minusDays(26).toString())
                    .narration("UPI/628192738/campus.wifi@axis/High-Speed Hostel Bandwidth")
                    .build());
            latestRecurringMap.put("Simpl BNPL Grocery Tab", TransactionEntity.builder()
                    .merchantPayee("Simpl")
                    .amount(BigDecimal.valueOf(650.00))
                    .category("BNPL_LIFESTYLE")
                    .transactionTimestamp(today.minusDays(28).toString())
                    .narration("UPI/192837465/simpl.pay@axis/BNPL Monthly Grocery Tab")
                    .build());
        }

        int offsetCounter = 2;
        for (Map.Entry<String, TransactionEntity> entry : latestRecurringMap.entrySet()) {
            TransactionEntity txn = entry.getValue();
            BigDecimal amount = txn.getAmount() != null ? txn.getAmount() : BigDecimal.valueOf(199.00);
            
            // Project renewal date ~30 days from last transaction or staggered in next 2-10 days
            LocalDate dueDate = today.plusDays(offsetCounter);
            offsetCounter += 3;
            long daysRemaining = ChronoUnit.DAYS.between(today, dueDate);

            if (daysRemaining <= lookaheadDays) {
                upcomingPayments.add(buildPaymentAlert(
                        entry.getKey(),
                        txn.getCategory() != null ? txn.getCategory() : "SUBSCRIPTIONS",
                        amount,
                        dueDate.toString(),
                        (int) daysRemaining,
                        fundingAccountNumber,
                        activeBalance,
                        minBalanceThreshold,
                        "Recurring auto-debit via UPI/e-Mandate. Ensure balance exceeds minimum requirement."
                ));
                totalUpcomingAmount = totalUpcomingAmount.add(amount);
            }
        }

        // Sort payments by days remaining (closest first)
        upcomingPayments.sort(Comparator.comparingInt(p -> (int) p.get("daysRemaining")));

        // Count warnings
        for (Map<String, Object> p : upcomingPayments) {
            String status = (String) p.get("status");
            if ("CRITICAL_INSUFFICIENT".equals(status)) {
                criticalCount++;
                totalAtRiskAmount = totalAtRiskAmount.add((BigDecimal) p.get("amount"));
            } else if ("WARNING_LOW_BALANCE".equals(status)) {
                warningCount++;
                totalAtRiskAmount = totalAtRiskAmount.add((BigDecimal) p.get("amount"));
            }
        }

        BigDecimal netBalanceAfterAll = activeBalance.subtract(totalUpcomingAmount);
        BigDecimal requiredTotalBuffer = totalUpcomingAmount.add(minBalanceThreshold);
        BigDecimal recommendedTopUp = requiredTotalBuffer.compareTo(activeBalance) > 0
                ? requiredTotalBuffer.subtract(activeBalance)
                : BigDecimal.ZERO;

        String overallStatus = criticalCount > 0 ? "CRITICAL_ACTION_REQUIRED"
                : (warningCount > 0 ? "WARNING_ACTION_RECOMMENDED" : "ALL_CLEAR_SAFE");

        String headline;
        if (criticalCount > 0) {
            headline = String.format("🚨 CRITICAL ALERT: %d auto-debit(s) will bounce due to insufficient funds! Maintain at least ₹%,.2f to protect your credit score.",
                    criticalCount, recommendedTopUp);
        } else if (warningCount > 0) {
            headline = String.format("⚠️ EARLY WARNING: %d auto-renewal(s) due soon. Your balance will drop below the ₹%,.2f minimum balance threshold.",
                    warningCount, minBalanceThreshold);
        } else {
            headline = String.format("✅ SAFE: Your balance of ₹%,.2f comfortably covers all upcoming renewals and maintains the ₹%,.2f minimum balance threshold.",
                    activeBalance, minBalanceThreshold);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("overallStatus", overallStatus);
        response.put("headline", headline);
        response.put("currentBalance", activeBalance);
        response.put("isSimulatedBalance", simulatedBalance != null);
        response.put("minimumBalanceThreshold", minBalanceThreshold);
        response.put("lookaheadDays", lookaheadDays);
        response.put("fundingAccountNumber", fundingAccountNumber);
        response.put("totalUpcomingDeductions", totalUpcomingAmount);
        response.put("totalAtRiskAmount", totalAtRiskAmount);
        response.put("criticalAlertsCount", criticalCount);
        response.put("lowBalanceWarningsCount", warningCount);
        response.put("activeWarningsCount", criticalCount + warningCount);
        response.put("netBalanceAfterAllDeductions", netBalanceAfterAll);
        response.put("recommendedTopUpAmount", recommendedTopUp);
        response.put("upcomingPayments", upcomingPayments);

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildPaymentAlert(
            String serviceName,
            String category,
            BigDecimal amount,
            String dueDate,
            int daysRemaining,
            String accountNumber,
            BigDecimal currentBalance,
            BigDecimal minThreshold,
            String note) {

        BigDecimal balanceAfter = currentBalance.subtract(amount);
        String status;
        String severity;
        String warningMessage;

        if (currentBalance.compareTo(amount) < 0) {
            status = "CRITICAL_INSUFFICIENT";
            severity = "CRITICAL";
            BigDecimal deficit = amount.subtract(currentBalance);
            BigDecimal neededDeposit = deficit.add(minThreshold);
            warningMessage = String.format("🚨 Critical: %s of ₹%,.2f is due in %d day%s. Your balance (₹%,.2f) is insufficient by ₹%,.2f! Deposit ₹%,.2f immediately to prevent auto-debit failure and bank penalty charges.",
                    serviceName, amount, daysRemaining, daysRemaining == 1 ? "" : "s", currentBalance, deficit, neededDeposit);
        } else if (balanceAfter.compareTo(minThreshold) < 0) {
            status = "WARNING_LOW_BALANCE";
            severity = "WARNING";
            BigDecimal dipBelow = minThreshold.subtract(balanceAfter);
            warningMessage = String.format("⚠️ Early Warning: %s renewal of ₹%,.2f is scheduled in %d day%s. Balance after deduction will drop to ₹%,.2f (₹%,.2f below the ₹%,.2f minimum balance threshold). Maintain minimum balance to prevent bank non-maintenance fines.",
                    serviceName, amount, daysRemaining, daysRemaining == 1 ? "" : "s", balanceAfter, dipBelow, minThreshold);
        } else {
            status = "SAFE";
            severity = "SAFE";
            warningMessage = String.format("✅ Balance is healthy. ₹%,.2f will be deducted in %d day%s. Projected balance after payment: ₹%,.2f (maintains minimum balance threshold).",
                    amount, daysRemaining, daysRemaining == 1 ? "" : "s", balanceAfter);
        }

        Map<String, Object> payment = new LinkedHashMap<>();
        payment.put("serviceName", serviceName);
        payment.put("category", category);
        payment.put("amount", amount);
        payment.put("dueDate", dueDate);
        payment.put("daysRemaining", daysRemaining);
        payment.put("linkedAccountNumber", accountNumber);
        payment.put("currentBalance", currentBalance);
        payment.put("projectedBalanceAfterDeduction", balanceAfter);
        payment.put("status", status);
        payment.put("severity", severity);
        payment.put("warningMessage", warningMessage);
        payment.put("advisoryNote", note);

        return payment;
    }
}
