package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HolderDto {
    private String name;

    @JsonProperty("Dob")
    private String dob;

    private String mobile;

    @JsonProperty("Nominee")
    private String nominee;

    private String landline;
    private String address;
    private String email;

    @JsonProperty("PAN")
    private String pan;

    private String ckycCompliance;

    public HolderDto() {}

    public HolderDto(String name, String dob, String mobile, String nominee,
                     String landline, String address, String email, String pan, String ckycCompliance) {
        this.name = name;
        this.dob = dob;
        this.mobile = mobile;
        this.nominee = nominee;
        this.landline = landline;
        this.address = address;
        this.email = email;
        this.pan = pan;
        this.ckycCompliance = ckycCompliance;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getNominee() { return nominee; }
    public void setNominee(String nominee) { this.nominee = nominee; }

    public String getLandline() { return landline; }
    public void setLandline(String landline) { this.landline = landline; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }

    public String getCkycCompliance() { return ckycCompliance; }
    public void setCkycCompliance(String ckycCompliance) { this.ckycCompliance = ckycCompliance; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String dob;
        private String mobile;
        private String nominee;
        private String landline;
        private String address;
        private String email;
        private String pan;
        private String ckycCompliance;

        public Builder name(String name) { this.name = name; return this; }
        public Builder dob(String dob) { this.dob = dob; return this; }
        public Builder mobile(String mobile) { this.mobile = mobile; return this; }
        public Builder nominee(String nominee) { this.nominee = nominee; return this; }
        public Builder landline(String landline) { this.landline = landline; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder pan(String pan) { this.pan = pan; return this; }
        public Builder ckycCompliance(String ckycCompliance) { this.ckycCompliance = ckycCompliance; return this; }

        public HolderDto build() {
            return new HolderDto(name, dob, mobile, nominee, landline, address, email, pan, ckycCompliance);
        }
    }
}
