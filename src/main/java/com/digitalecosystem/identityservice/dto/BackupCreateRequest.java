package com.digitalecosystem.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BackupCreateRequest {
    @NotBlank(message = "DID is required")
    private String did;

    private String passphrase;
}