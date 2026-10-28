package com.ledgr.sandbox.dto.rebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldersDto {
    @JsonProperty("Holder")
    private List<HolderDto> holders = new ArrayList<>();

    private String type;

    public HoldersDto() {}

    public HoldersDto(List<HolderDto> holders, String type) {
        this.holders = holders != null ? holders : new ArrayList<>();
        this.type = type;
    }

    public List<HolderDto> getHolders() { return holders; }
    public void setHolders(List<HolderDto> holders) { this.holders = holders; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<HolderDto> holders = new ArrayList<>();
        private String type;

        public Builder holders(List<HolderDto> holders) { this.holders = holders; return this; }
        public Builder type(String type) { this.type = type; return this; }

        public HoldersDto build() {
            return new HoldersDto(holders, type);
        }
    }
}
