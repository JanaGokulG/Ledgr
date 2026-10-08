package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentApprovalDto {
    private String otp = "123456";
    private String action = "APPROVE";
    private List<String> selectedAccountNumbers;

    public ConsentApprovalDto() {}

    public ConsentApprovalDto(String otp, String action, List<String> selectedAccountNumbers) {
        this.otp = otp != null ? otp : "123456";
        this.action = action != null ? action : "APPROVE";
        this.selectedAccountNumbers = selectedAccountNumbers;
    }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public List<String> getSelectedAccountNumbers() { return selectedAccountNumbers; }
    public void setSelectedAccountNumbers(List<String> selectedAccountNumbers) { this.selectedAccountNumbers = selectedAccountNumbers; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String otp = "123456";
        private String action = "APPROVE";
        private List<String> selectedAccountNumbers;

        public Builder otp(String otp) { this.otp = otp; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder selectedAccountNumbers(List<String> selectedAccountNumbers) { this.selectedAccountNumbers = selectedAccountNumbers; return this; }

        public ConsentApprovalDto build() {
            return new ConsentApprovalDto(otp, action, selectedAccountNumbers);
        }
    }
}
