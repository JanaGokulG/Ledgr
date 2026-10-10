package com.ledgr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "banking")
public class BankingProperties {

    private String clientId = DotenvLoader.get("BANKING_API_CLIENT_ID", DotenvLoader.get("PLAID_CLIENT_ID", ""));
    private String secret = DotenvLoader.get("BANKING_API_SECRET", DotenvLoader.get("PLAID_SECRET", ""));
    private String env = DotenvLoader.get("BANKING_API_ENV", DotenvLoader.get("PLAID_ENV", "sandbox"));
    private String baseUrl = DotenvLoader.get("BANKING_API_BASE_URL", DotenvLoader.get("PLAID_BASE_URL", "https://sandbox.plaid.com"));
    private String institutionId = DotenvLoader.get("BANKING_INSTITUTION_ID", DotenvLoader.get("PLAID_INSTITUTION_ID", "ins_20"));
    private String webhookUrl = DotenvLoader.get("BANKING_WEBHOOK_URL", DotenvLoader.get("PLAID_WEBHOOK_URL", "https://www.genericwebhookurl.com/webhook"));
    private String overrideUsername = DotenvLoader.get("BANKING_OVERRIDE_USERNAME", DotenvLoader.get("PLAID_OVERRIDE_USERNAME", "user_good"));
    private String overridePassword = DotenvLoader.get("BANKING_OVERRIDE_PASSWORD", DotenvLoader.get("PLAID_OVERRIDE_PASSWORD", "pass_good"));
    private List<String> initialProducts = new ArrayList<>(List.of(
            "auth", "transactions", "identity", "investments", "liabilities"
    ));
    private long simulatedDelayMs = 0L;
    private long transactionDelayMs = parseLongSafe(
            DotenvLoader.get("TRANSACTION_DELAY_MS", DotenvLoader.get("BANKING_TRANSACTION_DELAY_MS", "150")),
            150L
    );

    private static long parseLongSafe(String val, long defaultVal) {
        try {
            return (val != null && !val.isBlank()) ? Long.parseLong(val.trim()) : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
