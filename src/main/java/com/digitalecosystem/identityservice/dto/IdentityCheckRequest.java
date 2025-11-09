package com.digitalecosystem.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IdentityCheckRequest {
    @NotBlank(message = "Identifier is required")
    private String identifier;
}