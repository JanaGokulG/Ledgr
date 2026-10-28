package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountsWrapperDto {
    private List<AccountDataDto> accounts = new ArrayList<>();

    public AccountsWrapperDto() {}

    public AccountsWrapperDto(List<AccountDataDto> accounts) {
        this.accounts = accounts != null ? accounts : new ArrayList<>();
    }

    public List<AccountDataDto> getAccounts() { return accounts; }
    public void setAccounts(List<AccountDataDto> accounts) { this.accounts = accounts; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<AccountDataDto> accounts = new ArrayList<>();

        public Builder accounts(List<AccountDataDto> accounts) { this.accounts = accounts; return this; }

        public AccountsWrapperDto build() {
            return new AccountsWrapperDto(accounts);
        }
    }
}
