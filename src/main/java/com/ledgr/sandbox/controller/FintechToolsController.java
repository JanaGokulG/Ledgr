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
    private final com.ledgr.repository.RecurringRenewalRepository recurringRenewalRepository;

    public FintechToolsController(AccountRepository accountRepository,
                                  TransactionRepository transactionRepository,
                                  com.ledgr.repository.RecurringRenewalRepository recurringRenewalRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.recurringRenewalRepository = recurringRenewalRepository;
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
     * FEATURE 1: Auto-Renewal Early Warnings
     * Scans regular recurring subscriptions and scheduled auto-debits (Netflix, Spotify, Campus Wi-Fi,
     * Simpl BNPL, Gym, RD, etc.). Specifically warns before a week (<= 7 days) to maintain sufficient
     * balance in the funding account before execution date to prevent bounce penalties.
     */
    @GetMapping("/auto-renewals")
    public ResponseEntity<Map<String, Object>> getAutoRenewals(
            @RequestParam(required = false, defaultValue = "30") int lookaheadDays) {

        AccountEntity primaryAccount = getPrimarySavingsAccount();
        BigDecimal currentBalance = primaryAccount != null && primaryAccount.getCurrentBalance() != null
                ? primaryAccount.getCurrentBalance() : BigDecimal.ZERO;
        String fundingAccountNumber = primaryAccount != null ? primaryAccount.getAccountNumber() : "SB-1002938172";

        List<Map<String, Object>> allRenewals = getRecurringRenewalsList(fundingAccountNumber, currentBalance);

        // Filter by lookahead
        List<Map<String, Object>> filtered = new ArrayList<>();
        BigDecimal totalUpcomingAmount = BigDecimal.ZERO;
        BigDecimal dueWithinAWeekAmount = BigDecimal.ZERO;
        int dueWithinAWeekCount = 0;

        for (Map<String, Object> item : allRenewals) {
            int days = (int) item.get("daysRemaining");
            if (days <= lookaheadDays) {
                filtered.add(item);
                BigDecimal amt = (BigDecimal) item.get("amount");
                totalUpcomingAmount = totalUpcomingAmount.add(amt);
                boolean dueWithinWeek = (boolean) item.get("dueWithinAWeek");
                if (dueWithinWeek) {
                    dueWithinAWeekCount++;
                    dueWithinAWeekAmount = dueWithinAWeekAmount.add(amt);
                }
            }
        }

        boolean balanceSufficientForWeek = currentBalance.compareTo(dueWithinAWeekAmount) >= 0;
        BigDecimal weeklyDeficit = dueWithinAWeekAmount.compareTo(currentBalance) > 0
                ? dueWithinAWeekAmount.subtract(currentBalance)
                : BigDecimal.ZERO;

        String weeklyWarningHeadline;
        if (dueWithinAWeekCount > 0) {
            if (!balanceSufficientForWeek) {
                weeklyWarningHeadline = String.format(
                        "\uD83D\uDEA8 URGENT ACTION REQUIRED: %d auto-renewal(s) totaling \u20B9%,.2f are due within 7 days. Your balance (\u20B9%,.2f) is short by \u20B9%,.2f! Maintain sufficient balance immediately to prevent auto-debit bounce and penalty fees.",
                        dueWithinAWeekCount, dueWithinAWeekAmount, currentBalance, weeklyDeficit);
            } else {
                weeklyWarningHeadline = String.format(
                        "\u26A0\uFE0F EARLY WARNING (7-DAY NOTICE): %d auto-renewal(s) totaling \u20B9%,.2f are scheduled to debit within 7 days. Maintain at least this amount in %s before due dates to prevent bounce penalties.",
                        dueWithinAWeekCount, dueWithinAWeekAmount, fundingAccountNumber);
            }
        } else {
            weeklyWarningHeadline = "\u2705 All clear: No auto-renewals scheduled within the next 7 days.";
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("feature", "AUTO_RENEWAL_EARLY_WARNINGS");
        res.put("status", dueWithinAWeekCount > 0 ? (balanceSufficientForWeek ? "EARLY_WARNING_ACTIVE" : "CRITICAL_INSUFFICIENT_BALANCE") : "ALL_CLEAR");
        res.put("headline", weeklyWarningHeadline);
        res.put("fundingAccountNumber", fundingAccountNumber);
        res.put("currentBalance", currentBalance);
        res.put("totalTrackedRenewals", filtered.size());
        res.put("dueWithinAWeekCount", dueWithinAWeekCount);
        res.put("dueWithinAWeekTotalAmount", dueWithinAWeekAmount);
        res.put("totalLookaheadCommitment", totalUpcomingAmount);
        res.put("balanceSufficientForWeek", balanceSufficientForWeek);
        res.put("weeklyBalanceDeficit", weeklyDeficit);
        res.put("renewals", filtered);

        return ResponseEntity.ok(res);
    }

    /**
     * FEATURE 2: Minimum Balance Shield
     * Dedicated bank account protection engine that tracks the Minimum Average Balance (MAB) / Minimum Balance
     * requirement. Analyzes scheduled debits in the next 7 and 30 days, projects balance trajectory,
     * detects breach risks before they occur, and calculates required top-up to maintain the safety buffer.
     */
    @GetMapping("/min-balance-shield")
    public ResponseEntity<Map<String, Object>> getMinBalanceShield(
            @RequestParam(required = false, defaultValue = "1000") BigDecimal minBalanceThreshold,
            @RequestParam(required = false) BigDecimal simulatedBalance) {

        AccountEntity primaryAccount = getPrimarySavingsAccount();
        BigDecimal actualBalance = primaryAccount != null && primaryAccount.getCurrentBalance() != null
                ? primaryAccount.getCurrentBalance() : BigDecimal.ZERO;
        String fundingAccountNumber = primaryAccount != null ? primaryAccount.getAccountNumber() : "SB-1002938172";
        String bankName = primaryAccount != null && primaryAccount.getBranchName() != null ? "ICICI Bank (" + primaryAccount.getBranchName() + ")" : "Primary Bank";

        BigDecimal activeBalance = simulatedBalance != null ? simulatedBalance : actualBalance;

        List<Map<String, Object>> allRenewals = getRecurringRenewalsList(fundingAccountNumber, activeBalance);

        BigDecimal debitsNext7Days = BigDecimal.ZERO;
        BigDecimal debitsNext30Days = BigDecimal.ZERO;
        List<Map<String, Object>> threatTimeline = new ArrayList<>();

        BigDecimal runningBalance = activeBalance;
        for (Map<String, Object> r : allRenewals) {
            int days = (int) r.get("daysRemaining");
            BigDecimal amt = (BigDecimal) r.get("amount");
            if (days <= 30) {
                debitsNext30Days = debitsNext30Days.add(amt);
                if (days <= 7) {
                    debitsNext7Days = debitsNext7Days.add(amt);
                }
                runningBalance = runningBalance.subtract(amt);
                Map<String, Object> step = new LinkedHashMap<>();
                step.put("serviceName", r.get("serviceName"));
                step.put("amount", amt);
                step.put("dueDate", r.get("dueDate"));
                step.put("daysRemaining", days);
                step.put("dueWithinAWeek", r.get("dueWithinAWeek"));
                step.put("projectedBalanceAfter", runningBalance);
                step.put("breachesMinBalance", runningBalance.compareTo(minBalanceThreshold) < 0);
                threatTimeline.add(step);
            }
        }

        BigDecimal projectedBalance7Days = activeBalance.subtract(debitsNext7Days);
        BigDecimal projectedBalance30Days = activeBalance.subtract(debitsNext30Days);
        BigDecimal safetyBuffer7Days = projectedBalance7Days.subtract(minBalanceThreshold);
        BigDecimal safetyBuffer30Days = projectedBalance30Days.subtract(minBalanceThreshold);

        boolean breachRisk7Days = projectedBalance7Days.compareTo(minBalanceThreshold) < 0;
        boolean overdraftRisk7Days = projectedBalance7Days.compareTo(BigDecimal.ZERO) < 0;

        BigDecimal deficitAmount = breachRisk7Days ? minBalanceThreshold.subtract(projectedBalance7Days) : BigDecimal.ZERO;
        BigDecimal recommendedTopUp = debitsNext7Days.add(minBalanceThreshold).compareTo(activeBalance) > 0
                ? debitsNext7Days.add(minBalanceThreshold).subtract(activeBalance)
                : BigDecimal.ZERO;

        String shieldStatus;
        String statusHeadline;
        if (overdraftRisk7Days) {
            shieldStatus = "CRITICAL_OVERDRAFT_DEFICIT";
            statusHeadline = String.format(
                    "\uD83D\uDEA8 SHIELD CRITICAL: Upcoming 7-day debits (\u20B9%,.2f) will overdraft your account to -\u20B9%,.2f! Deposit \u20B9%,.2f immediately to restore the \u20B9%,.2f minimum balance.",
                    debitsNext7Days, projectedBalance7Days.abs(), recommendedTopUp, minBalanceThreshold);
        } else if (breachRisk7Days) {
            shieldStatus = "BREACH_RISK_DETECTED";
            statusHeadline = String.format(
                    "\u26A0\uFE0F SHIELD BREACH WARNING: Balance will drop to \u20B9%,.2f after next week's debits, which is \u20B9%,.2f below the \u20B9%,.2f minimum balance requirement. Top up \u20B9%,.2f to avoid bank non-maintenance fees.",
                    projectedBalance7Days, deficitAmount, minBalanceThreshold, recommendedTopUp);
        } else {
            shieldStatus = "SHIELD_SECURE";
            statusHeadline = String.format(
                    "\uD83D\uDEE1\uFE0F MINIMUM BALANCE SHIELD SECURE: Projected balance of \u20B9%,.2f maintains a robust safety buffer of \u20B9%,.2f above the \u20B9%,.2f threshold.",
                    projectedBalance7Days, safetyBuffer7Days, minBalanceThreshold);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("feature", "MINIMUM_BALANCE_SHIELD");
        res.put("shieldStatus", shieldStatus);
        res.put("statusHeadline", statusHeadline);
        res.put("accountNumber", fundingAccountNumber);
        res.put("bankName", bankName);
        res.put("currentBalance", activeBalance);
        res.put("isSimulatedBalance", simulatedBalance != null);
        res.put("minimumBalanceThreshold", minBalanceThreshold);
        res.put("upcomingDebitsNext7Days", debitsNext7Days);
        res.put("upcomingDebitsNext30Days", debitsNext30Days);
        res.put("projectedBalanceAfter7Days", projectedBalance7Days);
        res.put("projectedBalanceAfter30Days", projectedBalance30Days);
        res.put("safetyBuffer7Days", safetyBuffer7Days);
        res.put("safetyBuffer30Days", safetyBuffer30Days);
        res.put("breachRiskDetected", breachRisk7Days);
        res.put("deficitAmount", deficitAmount);
        res.put("recommendedTopUp", recommendedTopUp);
        res.put("threatTimeline", threatTimeline);

        return ResponseEntity.ok(res);
    }

    /**
     * Backwards-compatible combined endpoint for existing unit tests
     */
    @GetMapping("/renewal-warnings")
    public ResponseEntity<Map<String, Object>> getRenewalWarnings(
            @RequestParam(required = false, defaultValue = "1000") BigDecimal minBalanceThreshold,
            @RequestParam(required = false, defaultValue = "14") int lookaheadDays,
            @RequestParam(required = false) BigDecimal simulatedBalance) {

        AccountEntity primaryAccount = getPrimarySavingsAccount();
        BigDecimal actualLiquidBalance = primaryAccount != null && primaryAccount.getCurrentBalance() != null
                ? primaryAccount.getCurrentBalance() : BigDecimal.ZERO;
        String fundingAccountNumber = primaryAccount != null ? primaryAccount.getAccountNumber() : "SB-1002938172";

        BigDecimal activeBalance = simulatedBalance != null ? simulatedBalance : actualLiquidBalance;

        List<Map<String, Object>> rawList = getRecurringRenewalsList(fundingAccountNumber, activeBalance);
        List<Map<String, Object>> upcomingPayments = new ArrayList<>();
        BigDecimal totalUpcomingAmount = BigDecimal.ZERO;
        BigDecimal totalAtRiskAmount = BigDecimal.ZERO;
        int criticalCount = 0;
        int warningCount = 0;

        for (Map<String, Object> item : rawList) {
            int days = (int) item.get("daysRemaining");
            if (days <= lookaheadDays) {
                BigDecimal amt = (BigDecimal) item.get("amount");
                String name = (String) item.get("serviceName");
                String cat = (String) item.get("category");
                String dueDate = (String) item.get("dueDate");
                String note = (String) item.get("advisoryNote");

                Map<String, Object> p = buildPaymentAlert(name, cat, amt, dueDate, days, fundingAccountNumber, activeBalance, minBalanceThreshold, note);
                upcomingPayments.add(p);
                totalUpcomingAmount = totalUpcomingAmount.add(amt);

                String status = (String) p.get("status");
                if ("CRITICAL_INSUFFICIENT".equals(status)) {
                    criticalCount++;
                    totalAtRiskAmount = totalAtRiskAmount.add(amt);
                } else if ("WARNING_LOW_BALANCE".equals(status)) {
                    warningCount++;
                    totalAtRiskAmount = totalAtRiskAmount.add(amt);
                }
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
            headline = String.format("\uD83D\uDEA8 CRITICAL ALERT: %d auto-debit(s) will bounce due to insufficient funds! Maintain at least \u20B9%,.2f to protect your credit score.",
                    criticalCount, recommendedTopUp);
        } else if (warningCount > 0) {
            headline = String.format("\u26A0\uFE0F EARLY WARNING: %d auto-renewal(s) due soon. Your balance will drop below the \u20B9%,.2f minimum balance threshold.",
                    warningCount, minBalanceThreshold);
        } else {
            headline = String.format("\u2705 SAFE: Your balance of \u20B9%,.2f comfortably covers all upcoming renewals and maintains the \u20B9%,.2f minimum balance threshold.",
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

    private AccountEntity getPrimarySavingsAccount() {
        List<AccountEntity> accounts = accountRepository.findAll();
        AccountEntity primary = null;
        BigDecimal maxBal = BigDecimal.ZERO;
        for (AccountEntity acc : accounts) {
            BigDecimal bal = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : BigDecimal.ZERO;
            if ("SAVINGS".equalsIgnoreCase(acc.getAccountType()) || "DEPOSIT".equalsIgnoreCase(acc.getFiType())) {
                if (primary == null || bal.compareTo(maxBal) > 0) {
                    primary = acc;
                    maxBal = bal;
                }
            }
        }
        return primary;
    }

    @jakarta.annotation.PostConstruct
    public void initDatabaseRenewalsIfEmpty() {
        if (recurringRenewalRepository.count() == 0) {
            seedRenewalsToDatabase("SB-XXXXXXXX3329");
        }
    }

    public synchronized void seedRenewalsToDatabase(String fundingAccount) {
        LocalDate today = LocalDate.now();
        String acc = fundingAccount != null ? fundingAccount : "SB-XXXXXXXX3329";
        List<com.ledgr.entity.RecurringRenewalEntity> seeds = List.of(
                com.ledgr.entity.RecurringRenewalEntity.builder()
                        .serviceName("Netflix Student Plan")
                        .category("ENTERTAINMENT")
                        .amount(BigDecimal.valueOf(199.00))
                        .dueDate(today.plusDays(2))
                        .fundingAccountNumber(acc)
                        .billingCycle("MONTHLY")
                        .mandateType("UPI_AUTOPAY")
                        .advisoryNote("Monthly streaming subscription via UPI AutoPay. Ensure sufficient balance 7 days prior to prevent service interruption.")
                        .active(true)
                        .build(),
                com.ledgr.entity.RecurringRenewalEntity.builder()
                        .serviceName("Campus Wi-Fi & Maintenance")
                        .category("UTILITIES")
                        .amount(BigDecimal.valueOf(450.00))
                        .dueDate(today.plusDays(3))
                        .fundingAccountNumber(acc)
                        .billingCycle("MONTHLY")
                        .mandateType("CAMPUS_DIRECT_DEBIT")
                        .advisoryNote("Hostel high-speed fiber internet and portal access. Debited automatically by campus admin.")
                        .active(true)
                        .build(),
                com.ledgr.entity.RecurringRenewalEntity.builder()
                        .serviceName("Spotify Premium Student")
                        .category("ENTERTAINMENT")
                        .amount(BigDecimal.valueOf(59.00))
                        .dueDate(today.plusDays(4))
                        .fundingAccountNumber(acc)
                        .billingCycle("MONTHLY")
                        .mandateType("NACH_E_MANDATE")
                        .advisoryNote("Ad-free student audio subscription via NACH mandate. Bounces cause immediate plan cancellation.")
                        .active(true)
                        .build(),
                com.ledgr.entity.RecurringRenewalEntity.builder()
                        .serviceName("Simpl BNPL Grocery Tab")
                        .category("FINTECH_BNPL")
                        .amount(BigDecimal.valueOf(650.00))
                        .dueDate(today.plusDays(5))
                        .fundingAccountNumber(acc)
                        .billingCycle("MONTHLY")
                        .mandateType("UPI_AUTOPAY")
                        .advisoryNote("Bi-monthly pay-later bill for student canteen and Zepto orders. Late fee of \u20B9250 applies upon default.")
                        .active(true)
                        .build(),
                com.ledgr.entity.RecurringRenewalEntity.builder()
                        .serviceName("FitGym Student Pass")
                        .category("FITNESS_HEALTH")
                        .amount(BigDecimal.valueOf(1200.00))
                        .dueDate(today.plusDays(6))
                        .fundingAccountNumber(acc)
                        .billingCycle("MONTHLY")
                        .mandateType("DEBIT_CARD_RECURRING")
                        .advisoryNote("Monthly campus gym and fitness pass auto-debit.")
                        .active(true)
                        .build(),
                com.ledgr.entity.RecurringRenewalEntity.builder()
                        .serviceName("Amazon Prime Student")
                        .category("LIFESTYLE")
                        .amount(BigDecimal.valueOf(125.00))
                        .dueDate(today.plusDays(7))
                        .fundingAccountNumber(acc)
                        .billingCycle("ANNUAL_AMORTIZED")
                        .mandateType("UPI_AUTOPAY")
                        .advisoryNote("Annual student discount package amortized monthly. Free delivery and Prime Video access.")
                        .active(true)
                        .build(),
                com.ledgr.entity.RecurringRenewalEntity.builder()
                        .serviceName("Recurring Deposit (RD) Installment")
                        .category("BANKING_SAVINGS")
                        .amount(BigDecimal.valueOf(1000.00))
                        .dueDate(today.plusDays(12))
                        .fundingAccountNumber(acc)
                        .billingCycle("MONTHLY")
                        .mandateType("BANK_STANDING_INSTRUCTION")
                        .advisoryNote("Mandatory monthly bank RD installment. Bounces trigger RD forfeiture and \u20B9100 bank penalty.")
                        .active(true)
                        .build(),
                com.ledgr.entity.RecurringRenewalEntity.builder()
                        .serviceName("Student Term Health Cover")
                        .category("INSURANCE")
                        .amount(BigDecimal.valueOf(499.00))
                        .dueDate(today.plusDays(18))
                        .fundingAccountNumber(acc)
                        .billingCycle("MONTHLY")
                        .mandateType("NACH_E_MANDATE")
                        .advisoryNote("\u20B95,00,000 cashless medical insurance policy premium.")
                        .active(true)
                        .build(),
                com.ledgr.entity.RecurringRenewalEntity.builder()
                        .serviceName("GitHub Copilot & Cloud Sandbox")
                        .category("EDUCATION_TECH")
                        .amount(BigDecimal.valueOf(820.00))
                        .dueDate(today.plusDays(24))
                        .fundingAccountNumber(acc)
                        .billingCycle("MONTHLY")
                        .mandateType("INTERNATIONAL_DEBIT_MANDATE")
                        .advisoryNote("Developer workspace and AI cloud credits. Auto-renewed via international debit mandate.")
                        .active(true)
                        .build()
        );
        recurringRenewalRepository.saveAll(seeds);
    }

    /**
     * Loads recurring auto-renewals directly from the database table (recurring_renewals)
     */
    public List<Map<String, Object>> getRecurringRenewalsList(String fundingAccountNumber, BigDecimal currentBalance) {
        if (recurringRenewalRepository.count() == 0) {
            seedRenewalsToDatabase(fundingAccountNumber != null ? fundingAccountNumber : "SB-XXXXXXXX3329");
        }

        List<com.ledgr.entity.RecurringRenewalEntity> dbEntities = recurringRenewalRepository.findByActiveTrueOrderByDueDateAsc();
        List<Map<String, Object>> list = new ArrayList<>();

        for (com.ledgr.entity.RecurringRenewalEntity entity : dbEntities) {
            list.add(createRenewalMap(
                    entity.getServiceName(),
                    entity.getCategory(),
                    entity.getAmount(),
                    entity.getDueDate(),
                    entity.getFundingAccountNumber(),
                    currentBalance,
                    entity.getAdvisoryNote()
            ));
        }

        list.sort(Comparator.comparingInt(m -> (int) m.get("daysRemaining")));
        return list;
    }

    private Map<String, Object> createRenewalMap(
            String serviceName,
            String category,
            BigDecimal amount,
            LocalDate dueDate,
            String fundingAccount,
            BigDecimal currentBalance,
            String advisoryNote) {

        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        boolean dueWithinAWeek = daysRemaining <= 7;
        BigDecimal projectedBalanceAfter = currentBalance.subtract(amount);

        String warningMessage;
        String status;
        if (dueWithinAWeek) {
            status = "DUE_WITHIN_WEEK";
            if (currentBalance.compareTo(amount) < 0) {
                BigDecimal deficit = amount.subtract(currentBalance);
                warningMessage = String.format(
                        "\uD83D\uDEA8 CRITICAL WARNING (Due in %d day%s): %s of \u20B9%,.2f will debit on %s. Balance (\u20B9%,.2f) is short by \u20B9%,.2f! Maintain sufficient balance immediately.",
                        daysRemaining, daysRemaining == 1 ? "" : "s", serviceName, amount, dueDate, currentBalance, deficit);
            } else {
                warningMessage = String.format(
                        "\u26A0\uFE0F EARLY WARNING: %s of \u20B9%,.2f is scheduled for auto-debit in %d day%s on %s. Maintain at least \u20B9%,.2f in %s to ensure seamless execution and avoid bounce fees.",
                        serviceName, amount, daysRemaining, daysRemaining == 1 ? "" : "s", dueDate, amount, fundingAccount);
            }
        } else {
            status = "UPCOMING_SCHEDULED";
            warningMessage = String.format(
                    "\uD83D\uDCC5 Scheduled: %s of \u20B9%,.2f will be auto-debited in %d days on %s from account %s.",
                    serviceName, amount, daysRemaining, dueDate, fundingAccount);
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("serviceName", serviceName);
        map.put("category", category);
        map.put("amount", amount);
        map.put("dueDate", dueDate.toString());
        map.put("daysRemaining", (int) daysRemaining);
        map.put("dueWithinAWeek", dueWithinAWeek);
        map.put("fundingAccountNumber", fundingAccount);
        map.put("status", status);
        map.put("warningMessage", warningMessage);
        map.put("advisoryNote", advisoryNote);
        map.put("projectedBalanceAfterDeduction", projectedBalanceAfter);

        return map;
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
            warningMessage = String.format("\uD83D\uDEA8 Critical: %s of \u20B9%,.2f is due in %d day%s. Your balance (\u20B9%,.2f) is insufficient by \u20B9%,.2f! Deposit \u20B9%,.2f immediately to prevent auto-debit failure and bank penalty charges.",
                    serviceName, amount, daysRemaining, daysRemaining == 1 ? "" : "s", currentBalance, deficit, neededDeposit);
        } else if (balanceAfter.compareTo(minThreshold) < 0) {
            status = "WARNING_LOW_BALANCE";
            severity = "WARNING";
            BigDecimal dipBelow = minThreshold.subtract(balanceAfter);
            warningMessage = String.format("\u26A0\uFE0F Early Warning: %s renewal of \u20B9%,.2f is scheduled in %d day%s. Balance after deduction will drop to \u20B9%,.2f (\u20B9%,.2f below the \u20B9%,.2f minimum balance threshold). Maintain minimum balance to prevent bank non-maintenance fines.",
                    serviceName, amount, daysRemaining, daysRemaining == 1 ? "" : "s", balanceAfter, dipBelow, minThreshold);
        } else {
            status = "SAFE";
            severity = "SAFE";
            warningMessage = String.format("\u2705 Balance is healthy. \u20B9%,.2f will be deducted in %d day%s. Projected balance after payment: \u20B9%,.2f (maintains minimum balance threshold).",
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
