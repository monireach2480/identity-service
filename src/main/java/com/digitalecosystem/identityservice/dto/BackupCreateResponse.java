package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BackupCreateResponse {
    private String encryptedFile; // Base64 encoded
    private String fileHash;
}