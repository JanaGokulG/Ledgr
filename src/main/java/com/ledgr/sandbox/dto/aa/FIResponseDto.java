package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FIResponseDto {
    private String sessionId;
    private String consentId;
    private String status; // PENDING, READY, COMPLETED, FAILED
    @Builder.Default
    private String timestamp = Instant.now().toString();
}
