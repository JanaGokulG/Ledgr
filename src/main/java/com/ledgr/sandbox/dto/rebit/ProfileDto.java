package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileDto {
    @JsonProperty("Account")
    private AccountInfoDto account;

    @JsonProperty("Holders")
    private HoldersDto holders;

    public ProfileDto() {}

    public ProfileDto(AccountInfoDto account, HoldersDto holders) {
        this.account = account;
        this.holders = holders;
    }

    public AccountInfoDto getAccount() { return account; }
    public void setAccount(AccountInfoDto account) { this.account = account; }

    public HoldersDto getHolders() { return holders; }
    public void setHolders(HoldersDto holders) { this.holders = holders; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private AccountInfoDto account;
        private HoldersDto holders;

        public Builder account(AccountInfoDto account) { this.account = account; return this; }
        public Builder holders(HoldersDto holders) { this.holders = holders; return this; }

        public ProfileDto build() {
            return new ProfileDto(account, holders);
        }
    }
}
