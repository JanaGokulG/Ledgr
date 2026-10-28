package com.ledgr.sandbox.controller;

import com.ledgr.sandbox.dto.aa.*;
import com.ledgr.sandbox.dto.rebit.AccountsWrapperDto;
import com.ledgr.sandbox.entity.ConsentEntity;
import com.ledgr.sandbox.service.AccountAggregatorSandboxService;
import com.ledgr.sandbox.service.MockRebitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/aa")
@CrossOrigin(origins = "*")
public class AccountAggregatorSandboxController {

    private static final Logger log = LoggerFactory.getLogger(AccountAggregatorSandboxController.class);

    private final AccountAggregatorSandboxService aaSandboxService;
    private final MockRebitDataService mockRebitDataService;

    public AccountAggregatorSandboxController(AccountAggregatorSandboxService aaSandboxService,
                                              MockRebitDataService mockRebitDataService) {
        this.aaSandboxService = aaSandboxService;
        this.mockRebitDataService = mockRebitDataService;
    }

    @PostMapping("/consents")
    public ResponseEntity<ConsentResponseDto> createConsent(@RequestBody(required = false) ConsentRequestDto request) {
        if (request == null) {
            request = ConsentRequestDto.builder()
                    .customerId("student@setuaa")
                    .build();
        }
        ConsentResponseDto response = aaSandboxService.createConsent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/consents/{consentId}")
    public ResponseEntity<ConsentEntity> getConsentStatus(@PathVariable String consentId) {
        return ResponseEntity.ok(aaSandboxService.getConsent(consentId));
    }

    @PostMapping("/consents/{consentId}/approve")
    public ResponseEntity<Map<String, Object>> approveConsent(
            @PathVariable String consentId,
            @RequestBody(required = false) ConsentApprovalDto approvalDto) {
        if (approvalDto == null) {
            approvalDto = ConsentApprovalDto.builder().otp("123456").action("APPROVE").build();
        }
        ConsentEntity updated = aaSandboxService.approveConsent(consentId, approvalDto);
        return ResponseEntity.ok(Map.of(
                "consentId", updated.getId(),
                "status", updated.getStatus(),
                "message", "Consent marked as ACTIVE in Setu AA Sandbox."
        ));
    }

    @PostMapping("/consents/{consentId}/revoke")
    public ResponseEntity<Map<String, Object>> revokeConsent(@PathVariable String consentId) {
        ConsentEntity updated = aaSandboxService.revokeConsent(consentId);
        return ResponseEntity.ok(Map.of(
                "consentId", updated.getId(),
                "status", updated.getStatus(),
                "message", "Consent REVOKED."
        ));
    }

    @PostMapping("/fi/request")
    public ResponseEntity<FIResponseDto> createFiSession(@RequestBody FIRequestDto request) {
        FIResponseDto response = aaSandboxService.createFiSession(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fi/fetch/{sessionId}")
    public ResponseEntity<FIFetchResponseDto> fetchFiData(@PathVariable String sessionId) {
        FIFetchResponseDto response = aaSandboxService.fetchFiData(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mock/raw")
    public ResponseEntity<AccountsWrapperDto> getRawMockData() {
        return ResponseEntity.ok(mockRebitDataService.getFullPayload());
    }
}
