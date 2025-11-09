package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTPGenerateResponse {
    private String message;
    private Integer expiresIn;
    private String otpId;
}