package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class DIDStatusResponse {
    private String did;
    private String publicDid;
    private String didWebStatus;
    private LocalDateTime publishedAt;
    private Map<String, Object> metadata;
}