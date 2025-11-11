package com.digitalecosystem.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProofOfControlRequest {
    @NotBlank(message = "DID is required")
    private String did;

    @NotBlank(message = "Signature is required")
    private String signature;

    @NotBlank(message = "Nonce ID is required")
    private String nonceId;
}