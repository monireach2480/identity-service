package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DIDPublishResponse {
    private String status;
    private String publicDid;
    private String message;
    private String didWebStatus;
}