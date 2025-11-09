package com.digitalecosystem.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestoreIdentityRequest {
    @NotBlank(message = "Encrypted file is required")
    private String encryptedFile; // Base64 encoded

    private String passphrase;
}