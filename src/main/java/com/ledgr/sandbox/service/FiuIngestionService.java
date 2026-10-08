package com.ledgr.sandbox.service;

import com.ledgr.sandbox.dto.aa.*;
import com.ledgr.sandbox.dto.rebit.*;
import com.ledgr.sandbox.entity.AccountEntity;
import com.ledgr.sandbox.entity.TransactionEntity;
import com.ledgr.sandbox.repository.AccountRepository;
import com.ledgr.sandbox.repository.TransactionRepository;
import com.ledgr.sandbox.util.MerchantCategorizerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FiuIngestionService {

    private static final Logger log = LoggerFactory.getLogger(FiuIngestionService.class);

    private final AccountAggregatorSandboxService aaSandboxService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public FiuIngestionService(AccountAggregatorSandboxService aaSandboxService,
                               AccountRepository accountRepository,
                               TransactionRepository transactionRepository) {
        this.aaSandboxService = aaSandboxService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @FunctionalInterface
    public interface SyncProgressCallback {
        void onProgress(int currentStep, int totalSteps, String stepTitle, String message);
    }

    public Map<String, Object> executeFullSyncWithRealisticDelay(String customerVpa, SyncProgressCallback callback) {
        log.info("Starting realistic multi-phase AA Sandbox Handshake for customer: {}", customerVpa);
        final int TOTAL_STEPS = 5;

        // Step 1: Consent Negotiation
        if (callback != null) callback.onProgress(1, TOTAL_STEPS, "1/5 Initiating AA Consent", "Dispatching ReBIT consent request to Setu AA Gateway...");
        sleep(100);

        ConsentRequestDto consentReq = ConsentRequestDto.builder()
                .customerId(customerVpa != null ? customerVpa : "student@setuaa")
                .consentTypes(List.of("PROFILE", "SUMMARY", "TRANSACTIONS"))
                .fiTypes(List.of("DEPOSIT", "TERM_DEPOSIT", "RECURRING_DEPOSIT"))
                .dateRange(ConsentRequestDto.DateRangeDto.builder()
                        .from("2019-12-01")
                        .to("2021-01-01")
                        .build())
                .build();
        ConsentResponseDto consentRes = aaSandboxService.createConsent(consentReq);

        // Step 2: OTP & Digital Signature Verification
        if (callback != null) callback.onProgress(2, TOTAL_STEPS, "2/5 Verifying Student OTP & Digital Signature", "Verifying 6-digit OTP (123456) & generating ReBIT Consent Artifact...");
        sleep(100);
        aaSandboxService.approveConsent(consentRes.getId(), ConsentApprovalDto.builder().otp("123456").action("APPROVE").build());

        // Step 3: FIP Bank Data Session
        if (callback != null) callback.onProgress(3, TOTAL_STEPS, "3/5 Querying Banking Provider (ICICI Bank FIP)", "Negotiating ECDH Curve25519 key-exchange with bank server...");
        sleep(100);
        FIRequestDto fiReq = FIRequestDto.builder()
                .consentId(consentRes.getId())
                .dateTimeRange(consentReq.getDateRange())
                .build();
        FIResponseDto fiRes = aaSandboxService.createFiSession(fiReq);

        // Step 4: Statement Fetch & Decryption
        if (callback != null) callback.onProgress(4, TOTAL_STEPS, "4/5 Decrypting Statement JSON Payload", "Downloading 256-bit encrypted statement packet & deserializing ReBIT schema...");
        sleep(100);
        FIFetchResponseDto fetchRes = aaSandboxService.fetchFiData(fiRes.getSessionId());

        // Step 5: Ledger Ingestion & Categorization
        if (callback != null) callback.onProgress(5, TOTAL_STEPS, "5/5 Normalizing & Ingesting into Database", "Persisting accounts and categorizing transactions into database...");
        IngestionResult result = ingestRebitData(fetchRes.getPayload());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("consentId", consentRes.getId());
        response.put("sessionId", fiRes.getSessionId());
        response.put("accountsSynchronized", result.accountsCount());
        response.put("transactionsSynchronized", result.transactionsCount());
        response.put("totalLiquidBalance", result.totalLiquidBalance());
        response.put("totalFixedDepositAssets", result.totalFdAssets());
        response.put("totalRecurringDepositAssets", result.totalRdAssets());
        response.put("summary", "Realistic AA Handshake completed with database synchronization.");

        return response;
    }

    @Transactional
    public Map<String, Object> executeFullSync(String customerVpa) {
        return executeFullSyncWithRealisticDelay(customerVpa, null);
    }

    public record IngestionResult(int accountsCount, int transactionsCount, BigDecimal totalLiquidBalance, BigDecimal totalFdAssets, BigDecimal totalRdAssets) {}

    @Transactional
    public IngestionResult ingestRebitData(AccountsWrapperDto wrapperDto) {
        if (wrapperDto == null || wrapperDto.getAccounts() == null) {
            return new IngestionResult(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        int totalTxnCount = 0;
        BigDecimal liquidBalance = BigDecimal.ZERO;
        BigDecimal fdAssets = BigDecimal.ZERO;
        BigDecimal rdAssets = BigDecimal.ZERO;

        final String TARGET_USER_NAME = "Akshay Kumar";
        final String TARGET_USER_PAN = "AIMPN5746M";
        final String TARGET_USER_MOBILE = "8008233551";
        final String TARGET_USER_EMAIL = "akshayku@gmail.com";
        final String TARGET_USER_ADDRESS = "8/1190, 5th Cross, 3rd Main, 7th Block, Jayanagar, Bangalore - 560011";

        for (AccountDataDto accDto : wrapperDto.getAccounts()) {
            ProfileDto profile = accDto.getProfile();
            SummaryDto summary = accDto.getSummary();

            String accNumber = (profile != null && profile.getAccount() != null) ? profile.getAccount().getNumber() : "UNKNOWN";
            String fiType = (profile != null && profile.getAccount() != null) ? profile.getAccount().getFitype() : "DEPOSIT";
            String accType = (profile != null && profile.getAccount() != null) ? profile.getAccount().getAcctype() : "SAVINGS";

            AccountEntity account = accountRepository.findByAccountNumber(accNumber)
                    .orElseGet(() -> AccountEntity.builder().accountNumber(accNumber).build());

            account.setFiType(fiType);
            account.setAccountType(summary != null && summary.getType() != null ? summary.getType() : accType);

            // Enforce single-user personal finance ownership (Akshay Kumar)
            account.setHolderName(TARGET_USER_NAME);
            account.setHolderPan(TARGET_USER_PAN);
            account.setHolderMobile(TARGET_USER_MOBILE);
            account.setHolderEmail(TARGET_USER_EMAIL);
            account.setHolderAddress(TARGET_USER_ADDRESS);
            account.setNomineeStatus("REGISTERED");
            account.setCkycCompliant(true);

            if (summary != null) {
                BigDecimal bal = parseBigDecimal(summary.getCurrentBalance());
                account.setCurrentBalance(bal);
                account.setCurrency(summary.getCurrency() != null ? summary.getCurrency().trim() : "INR");
                account.setIfsc(summary.getIfsc());
                account.setBranchName(summary.getBranch());
                account.setMicrCode(summary.getMicrCode());
                account.setOpeningDate(summary.getOpeningDate());
                account.setStatus(summary.getStatus());

                if ("SAVINGS".equalsIgnoreCase(account.getAccountType()) || "DEPOSIT".equalsIgnoreCase(fiType)) {
                    liquidBalance = liquidBalance.add(bal);
                }

                if (summary.getMaturityAmount() != null) {
                    account.setMaturityAmount(parseBigDecimal(summary.getMaturityAmount()));
                }
                account.setMaturityDate(summary.getMaturityDate());
                if (summary.getInterestRate() != null) {
                    account.setInterestRate(parseBigDecimal(summary.getInterestRate()));
                }
                if (summary.getPrincipalAmount() != null) {
                    account.setPrincipalAmount(parseBigDecimal(summary.getPrincipalAmount()));
                }
                if (summary.getTenureDays() != null && !summary.getTenureDays().isBlank()) {
                    try { account.setTenureDays(Integer.parseInt(summary.getTenureDays())); } catch (NumberFormatException ignored) {}
                }
                if (summary.getTenureMonths() != null && !summary.getTenureMonths().isBlank()) {
                    try { account.setTenureMonths(Integer.parseInt(summary.getTenureMonths())); } catch (NumberFormatException ignored) {}
                }
                account.setCompoundingFrequency(summary.getCompoundingFrequency());
                account.setInterestComputation(summary.getInterestComputation());

                if ("FIXED".equalsIgnoreCase(account.getAccountType()) || "TERM-DEPOSIT".equalsIgnoreCase(fiType)) {
                    fdAssets = fdAssets.add(bal != null ? bal : parseBigDecimal(summary.getCurrentValue()));
                }

                if (summary.getRecurringAmount() != null) {
                    account.setRecurringAmount(parseBigDecimal(summary.getRecurringAmount()));
                }
                if (summary.getRecurringDepositDay() != null && !summary.getRecurringDepositDay().isBlank()) {
                    try { account.setRecurringDepositDay(Integer.parseInt(summary.getRecurringDepositDay())); } catch (NumberFormatException ignored) {}
                }
                if ("RECURRING".equalsIgnoreCase(account.getAccountType()) || "RECURRING_DEPOSIT".equalsIgnoreCase(fiType)) {
                    rdAssets = rdAssets.add(bal != null ? bal : parseBigDecimal(summary.getCurrentValue()));
                }
            }

            account.setLastSyncedAt(LocalDateTime.now());
            AccountEntity savedAccount = accountRepository.save(account);

            if (accDto.getTransactions() != null && !accDto.getTransactions().isEmpty()) {
                savedAccount.getTransactions().clear();

                for (TransactionDto txnDto : accDto.getTransactions()) {
                    var catResult = MerchantCategorizerUtil.categorize(
                            txnDto.getNarration(),
                            txnDto.getType(),
                            txnDto.getMode()
                    );

                    TransactionEntity txnEntity = TransactionEntity.builder()
                            .account(savedAccount)
                            .txnId(txnDto.getTxnId())
                            .txnType(txnDto.getType() != null ? txnDto.getType().toUpperCase() : "DEBIT")
                            .paymentMode(txnDto.getMode() != null ? txnDto.getMode().toUpperCase() : "OTHERS")
                            .amount(parseBigDecimal(txnDto.getAmount()))
                            .balanceAfterTxn(parseBigDecimal(txnDto.getCurrentBalance()))
                            .transactionTimestamp(txnDto.getTransactionTimestamp())
                            .valueDate(txnDto.getValueDate())
                            .narration(txnDto.getNarration())
                            .referenceNumber(txnDto.getReference())
                            .category(catResult.category())
                            .merchantPayee(catResult.merchantPayee())
                            .build();

                    savedAccount.getTransactions().add(txnEntity);
                    totalTxnCount++;
                }
                accountRepository.save(savedAccount);
            }
        }

        return new IngestionResult(wrapperDto.getAccounts().size(), totalTxnCount, liquidBalance, fdAssets, rdAssets);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private BigDecimal parseBigDecimal(String str) {
        if (str == null || str.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(str.trim().replaceAll(",", ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
