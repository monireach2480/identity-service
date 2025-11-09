package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DIDCreateResponse {
    private String did;
    private String status;
    private LocalDateTime timestamp;
}