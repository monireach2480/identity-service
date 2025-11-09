package com.digitalecosystem.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class DIDCreateRequest {
    @NotBlank(message = "DID is required")
    private String did;

    @NotBlank(message = "Public key is required")
    private String publicKey;

    private UUID deviceId;

    private Boolean offlineCreated = false;

    private Long clientTimestamp;
}