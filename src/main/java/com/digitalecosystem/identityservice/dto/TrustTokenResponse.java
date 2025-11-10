package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrustTokenResponse {
    private String status;
    private String message;
}