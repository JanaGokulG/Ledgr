package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
