package com.ledgr.sandbox.service;

import com.ledgr.sandbox.dto.aa.*;
import com.ledgr.sandbox.dto.rebit.AccountsWrapperDto;
import com.ledgr.sandbox.entity.ConsentEntity;
import com.ledgr.sandbox.entity.FiSessionEntity;
import com.ledgr.sandbox.repository.ConsentRepository;
import com.ledgr.sandbox.repository.FiSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountAggregatorSandboxService {

    private static final Logger log = LoggerFactory.getLogger(AccountAggregatorSandboxService.class);

    private final ConsentRepository consentRepository;
    private final FiSessionRepository fiSessionRepository;
    private final MockRebitDataService mockRebitDataService;

    @Value("${ledgr.sandbox.fiu-id:LEDGR_FIU_IN}")
    private String fiuId;

    public AccountAggregatorSandboxService(ConsentRepository consentRepository,
                                           FiSessionRepository fiSessionRepository,
                                           MockRebitDataService mockRebitDataService) {
        this.consentRepository = consentRepository;
        this.fiSessionRepository = fiSessionRepository;
        this.mockRebitDataService = mockRebitDataService;
    }

    @Transactional
    public ConsentResponseDto createConsent(ConsentRequestDto request) {
        String consentId = UUID.randomUUID().toString();
        String approvalUrl = "http://localhost:8085/api/v1/aa/consents/" + consentId + "/approve";

        ConsentEntity entity = ConsentEntity.builder()
                .id(consentId)
                .customerId(request.getCustomerId() != null ? request.getCustomerId() : "student@setuaa")
                .status("PENDING")
                .consentTypes(request.getConsentTypes() != null ? String.join(",", request.getConsentTypes()) : "PROFILE,SUMMARY,TRANSACTIONS")
                .fiTypes(request.getFiTypes() != null ? String.join(",", request.getFiTypes()) : "DEPOSIT,TERM_DEPOSIT,RECURRING_DEPOSIT")
                .dateRangeFrom(request.getDateRange() != null ? request.getDateRange().getFrom() : "2019-12-01")
                .dateRangeTo(request.getDateRange() != null ? request.getDateRange().getTo() : "2021-01-01")
                .redirectUrl(request.getRedirectUrl())
                .callbackUrl(request.getCallbackUrl())
                .build();

        consentRepository.save(entity);
        log.info("Created AA Sandbox Consent: {} for customer: {}", consentId, entity.getCustomerId());

        return ConsentResponseDto.builder()
                .id(consentId)
                .status("PENDING")
                .url(approvalUrl)
                .customerId(entity.getCustomerId())
                .consentTypes(request.getConsentTypes())
                .fiTypes(request.getFiTypes())
                .build();
    }

    public ConsentEntity getConsent(String consentId) {
        return consentRepository.findById(consentId)
                .orElseThrow(() -> new IllegalArgumentException("Consent not found for ID: " + consentId));
    }

    @Transactional
    public ConsentEntity approveConsent(String consentId, ConsentApprovalDto approvalDto) {
        ConsentEntity entity = getConsent(consentId);
        if ("REJECT".equalsIgnoreCase(approvalDto.getAction())) {
            entity.setStatus("REJECTED");
            log.info("Consent {} REJECTED by customer simulation.", consentId);
        } else {
            entity.setStatus("ACTIVE");
            log.info("Consent {} APPROVED (OTP verified) and marked ACTIVE.", consentId);
        }
        return consentRepository.save(entity);
    }

    @Transactional
    public ConsentEntity revokeConsent(String consentId) {
        ConsentEntity entity = getConsent(consentId);
        entity.setStatus("REVOKED");
        log.info("Consent {} REVOKED by customer.", consentId);
        return consentRepository.save(entity);
    }

    @Transactional
    public FIResponseDto createFiSession(FIRequestDto request) {
        ConsentEntity consent = getConsent(request.getConsentId());
        if (!"ACTIVE".equalsIgnoreCase(consent.getStatus())) {
            throw new IllegalStateException("Cannot create FI session. Consent status is: " + consent.getStatus() + ". Must be ACTIVE.");
        }

        String sessionId = UUID.randomUUID().toString();
        FiSessionEntity session = FiSessionEntity.builder()
                .sessionId(sessionId)
                .consentId(request.getConsentId())
                .status("READY")
                .dateRangeFrom(request.getDateTimeRange() != null ? request.getDateTimeRange().getFrom() : consent.getDateRangeFrom())
                .dateRangeTo(request.getDateTimeRange() != null ? request.getDateTimeRange().getTo() : consent.getDateRangeTo())
                .build();

        fiSessionRepository.save(session);
        log.info("Created FI Session: {} for Consent: {}", sessionId, request.getConsentId());

        return FIResponseDto.builder()
                .sessionId(sessionId)
                .consentId(request.getConsentId())
                .status("READY")
                .build();
    }

    @Transactional
    public FIFetchResponseDto fetchFiData(String sessionId) {
        FiSessionEntity session = fiSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("FI Session not found for ID: " + sessionId));

        if ("FAILED".equalsIgnoreCase(session.getStatus())) {
            throw new IllegalStateException("FI Session " + sessionId + " has FAILED.");
        }

        session.setStatus("COMPLETED");
        fiSessionRepository.save(session);

        AccountsWrapperDto rawAccounts = mockRebitDataService.getFullPayload();

        return FIFetchResponseDto.builder()
                .sessionId(sessionId)
                .consentId(session.getConsentId())
                .status("COMPLETED")
                .fiuId(fiuId)
                .payload(rawAccounts)
                .build();
    }
}
