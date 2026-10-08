package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountInfoDto {
    private String number;
    private String acctype;
    private String fitype;

    public AccountInfoDto() {}

    public AccountInfoDto(String number, String acctype, String fitype) {
        this.number = number;
        this.acctype = acctype;
        this.fitype = fitype;
    }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getAcctype() { return acctype; }
    public void setAcctype(String acctype) { this.acctype = acctype; }

    public String getFitype() { return fitype; }
    public void setFitype(String fitype) { this.fitype = fitype; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String number;
        private String acctype;
        private String fitype;

        public Builder number(String number) { this.number = number; return this; }
        public Builder acctype(String acctype) { this.acctype = acctype; return this; }
        public Builder fitype(String fitype) { this.fitype = fitype; return this; }

        public AccountInfoDto build() {
            return new AccountInfoDto(number, acctype, fitype);
        }
    }
}
