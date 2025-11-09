package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdentityCheckResponse {
    private Boolean exists;
    private String did;
}