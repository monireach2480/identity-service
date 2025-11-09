package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestoreIdentityResponse {
    private String status;
    private String did;
    private String message;
}