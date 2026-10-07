package com.ledgr.sandbox.util;

import java.util.Locale;

public class MerchantCategorizerUtil {

    public record CategorizationResult(String category, String merchantPayee) {}

    public static CategorizationResult categorize(String narration, String txnType, String mode) {
        if (narration == null) {
            narration = "";
        }
        String lower = narration.toLowerCase(Locale.ROOT);

        // Term Deposit / RD Opening / Installments
        if (lower.contains("towards rd") || lower.contains("rd installment")) {
            return new CategorizationResult("SAVINGS_RD", "Recurring Deposit");
        }
        if (lower.contains("towards fd") || lower.contains("fd opening")) {
            return new CategorizationResult("INVESTMENTS_FD", "Fixed Deposit");
        }

        // Mutual Funds / Investments / Insurance
        if (lower.contains("mutual fund") || lower.contains("motilal oswal") || lower.contains("sbi mutual")) {
            return new CategorizationResult("INVESTMENTS_MUTUAL_FUNDS", "Mutual Funds");
        }
        if (lower.contains("nps trust")) {
            return new CategorizationResult("INVESTMENTS_NPS", "NPS Trust");
        }
        if (lower.contains("kotak life") || lower.contains("lic") || lower.contains("insurance")) {
            return new CategorizationResult("INSURANCE", "Kotak Life Insurance");
        }
        if (lower.contains("hdfcltd")) {
            return new CategorizationResult("LOAN_HOUSING", "HDFC Housing");
        }

        // Food & Dining / Delivery
        if (lower.contains("swiggy")) {
            return new CategorizationResult("FOOD_DINING", "Swiggy");
        }
        if (lower.contains("zomato")) {
            return new CategorizationResult("FOOD_DINING", "Zomato");
        }

        // Commute / Travel
        if (lower.contains("ola") || lower.contains("uber")) {
            return new CategorizationResult("TRAVEL_COMMUTE", "Ola / Uber");
        }

        // Shopping & Subscriptions / BNPL
        if (lower.contains("simpl") || lower.contains("getsimpl")) {
            return new CategorizationResult("BNPL_LIFESTYLE", "Simpl");
        }
        if (lower.contains("amzn") || lower.contains("amazon")) {
            return new CategorizationResult("SHOPPING_ECOMMERCE", "Amazon");
        }
        if (lower.contains("bbbp") || lower.contains("mariya mart") || lower.contains("mart") || lower.contains("levitate")) {
            return new CategorizationResult("GROCERIES_SHOPPING", "Local Store / Supermarket");
        }

        // ATM / Cash
        if (lower.contains("cash wdl") || "ATM".equalsIgnoreCase(mode) || lower.contains("nfs")) {
            return new CategorizationResult("ATM_CASH_WITHDRAWAL", "ATM Cash Withdrawal");
        }

        // Credit Cards
        if (lower.contains("sbicard") || lower.contains("credit card")) {
            return new CategorizationResult("CREDIT_CARD_BILL", "SBI Card");
        }

        // Credit / Salary / Scholarship / Transfers
        if ("CREDIT".equalsIgnoreCase(txnType)) {
            if (lower.contains("scholarship") || lower.contains("stipend")) {
                return new CategorizationResult("SCHOLARSHIP_STIPEND", "Education Fund");
            }
            if (lower.contains("salary") || lower.contains("payroll")) {
                return new CategorizationResult("INCOME_SALARY", "Salary / Freelance");
            }
            return new CategorizationResult("INCOMING_TRANSFER", "Bank Transfer");
        }

        return new CategorizationResult("GENERAL_EXPENSE", "General Merchant");
    }
}
