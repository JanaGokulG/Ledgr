package com.ledgr.sandbox.dto.aa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentApprovalDto {
    @Builder.Default
    private String otp = "123456"; // Default mock OTP
    @Builder.Default
    private String action = "APPROVE"; // APPROVE or REJECT
    private List<String> selectedAccountNumbers; // optional: specify accounts linked
}
