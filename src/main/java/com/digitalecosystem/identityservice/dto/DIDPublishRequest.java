package com.digitalecosystem.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DIDPublishRequest {
    @NotBlank(message = "DID is required")
    private String did;

    @NotBlank(message = "Domain is required")
    private String domain;

    private String path; // optional - defaults to users:{userId}
}
