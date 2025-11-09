package com.digitalecosystem.identityservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OTPGenerateRequest {
    @NotBlank(message = "Identifier is required")
    private String identifier;

    @Pattern(regexp = "email|phone", message = "Channel must be 'email' or 'phone'")
    private String channel;
}