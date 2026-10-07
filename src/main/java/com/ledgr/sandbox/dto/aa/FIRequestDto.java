package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FIRequestDto {
    private String consentId;
    private ConsentRequestDto.DateRangeDto dateTimeRange;
    private KeyMaterialDto keyMaterial;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyMaterialDto {
        private String cryptoAlg; // ECDH
        private String curve;     // Curve25519
        private String params;
        private KeyDto DHPublicKey;
        private String Nonce;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyDto {
        private String expiry;
        private String Parameters;
        private String KeyValue;
    }
}
