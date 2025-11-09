package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTPValidateResponse {
    private String status;
    private String message;
    private String accessToken;
}