package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ledgr.sandbox.dto.rebit.AccountsWrapperDto;
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
public class FIFetchResponseDto {
    private String sessionId;
    private String consentId;
    private String status; // COMPLETED
    @Builder.Default
    private String timestamp = Instant.now().toString();
    private String fiuId;
    
    // In Setu AA, the raw decrypted or parsed ReBIT payload
    private AccountsWrapperDto payload;
}
