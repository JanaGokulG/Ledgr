package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentResponseDto {
    private String id; // consent ID
    private String status; // PENDING, ACTIVE, REJECTED, REVOKED, EXPIRED
    private String url; // Redirect URL for Sandbox OTP Approval
    private String customerId;
    private List<String> consentTypes;
    private List<String> fiTypes;
    @Builder.Default
    private String createdAt = Instant.now().toString();
}
