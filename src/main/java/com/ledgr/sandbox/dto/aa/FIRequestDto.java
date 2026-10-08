package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FIRequestDto {
    private String consentId;
    private ConsentRequestDto.DateRangeDto dateTimeRange;
    private KeyMaterialDto keyMaterial;

    public FIRequestDto() {}

    public FIRequestDto(String consentId, ConsentRequestDto.DateRangeDto dateTimeRange, KeyMaterialDto keyMaterial) {
        this.consentId = consentId;
        this.dateTimeRange = dateTimeRange;
        this.keyMaterial = keyMaterial;
    }

    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }

    public ConsentRequestDto.DateRangeDto getDateTimeRange() { return dateTimeRange; }
    public void setDateTimeRange(ConsentRequestDto.DateRangeDto dateTimeRange) { this.dateTimeRange = dateTimeRange; }

    public KeyMaterialDto getKeyMaterial() { return keyMaterial; }
    public void setKeyMaterial(KeyMaterialDto keyMaterial) { this.keyMaterial = keyMaterial; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String consentId;
        private ConsentRequestDto.DateRangeDto dateTimeRange;
        private KeyMaterialDto keyMaterial;

        public Builder consentId(String consentId) { this.consentId = consentId; return this; }
        public Builder dateTimeRange(ConsentRequestDto.DateRangeDto dateTimeRange) { this.dateTimeRange = dateTimeRange; return this; }
        public Builder keyMaterial(KeyMaterialDto keyMaterial) { this.keyMaterial = keyMaterial; return this; }

        public FIRequestDto build() {
            return new FIRequestDto(consentId, dateTimeRange, keyMaterial);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeyMaterialDto {
        private String cryptoAlg;
        private String curve;
        private String params;
        private KeyDto DHPublicKey;
        private String Nonce;

        public KeyMaterialDto() {}
        public KeyMaterialDto(String cryptoAlg, String curve, String params, KeyDto DHPublicKey, String Nonce) {
            this.cryptoAlg = cryptoAlg;
            this.curve = curve;
            this.params = params;
            this.DHPublicKey = DHPublicKey;
            this.Nonce = Nonce;
        }

        public String getCryptoAlg() { return cryptoAlg; }
        public void setCryptoAlg(String cryptoAlg) { this.cryptoAlg = cryptoAlg; }

        public String getCurve() { return curve; }
        public void setCurve(String curve) { this.curve = curve; }

        public String getParams() { return params; }
        public void setParams(String params) { this.params = params; }

        public KeyDto getDHPublicKey() { return DHPublicKey; }
        public void setDHPublicKey(KeyDto DHPublicKey) { this.DHPublicKey = DHPublicKey; }

        public String getNonce() { return Nonce; }
        public void setNonce(String Nonce) { this.Nonce = Nonce; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String cryptoAlg;
            private String curve;
            private String params;
            private KeyDto DHPublicKey;
            private String Nonce;

            public Builder cryptoAlg(String cryptoAlg) { this.cryptoAlg = cryptoAlg; return this; }
            public Builder curve(String curve) { this.curve = curve; return this; }
            public Builder params(String params) { this.params = params; return this; }
            public Builder DHPublicKey(KeyDto DHPublicKey) { this.DHPublicKey = DHPublicKey; return this; }
            public Builder Nonce(String Nonce) { this.Nonce = Nonce; return this; }

            public KeyMaterialDto build() {
                return new KeyMaterialDto(cryptoAlg, curve, params, DHPublicKey, Nonce);
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeyDto {
        private String expiry;
        private String Parameters;
        private String KeyValue;

        public KeyDto() {}
        public KeyDto(String expiry, String Parameters, String KeyValue) {
            this.expiry = expiry;
            this.Parameters = Parameters;
            this.KeyValue = KeyValue;
        }

        public String getExpiry() { return expiry; }
        public void setExpiry(String expiry) { this.expiry = expiry; }

        public String getParameters() { return Parameters; }
        public void setParameters(String Parameters) { this.Parameters = Parameters; }

        public String getKeyValue() { return KeyValue; }
        public void setKeyValue(String KeyValue) { this.KeyValue = KeyValue; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String expiry;
            private String Parameters;
            private String KeyValue;

            public Builder expiry(String expiry) { this.expiry = expiry; return this; }
            public Builder Parameters(String Parameters) { this.Parameters = Parameters; return this; }
            public Builder KeyValue(String KeyValue) { this.KeyValue = KeyValue; return this; }

            public KeyDto build() {
                return new KeyDto(expiry, Parameters, KeyValue);
            }
        }
    }
}
