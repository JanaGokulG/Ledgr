package com.ledgr.sandbox.controller;

import com.ledgr.entity.AccountEntity;
import com.ledgr.entity.TransactionEntity;
import com.ledgr.repository.AccountRepository;
import com.ledgr.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
}
