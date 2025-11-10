package com.digitalecosystem.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

@Data
public class TrustTokenRequest {
    @NotBlank(message = "DID is required")
    private String did;

    private Map<String, Object> deviceInfo;
}