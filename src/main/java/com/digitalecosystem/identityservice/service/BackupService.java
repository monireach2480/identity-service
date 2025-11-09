package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.dto.BackupCreateRequest;
import com.digitalecosystem.identityservice.dto.BackupCreateResponse;
import com.digitalecosystem.identityservice.dto.RestoreIdentityRequest;
import com.digitalecosystem.identityservice.dto.RestoreIdentityResponse;
import com.digitalecosystem.identityservice.entity.BackupMetadata;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.digitalecosystem.identityservice.exception.IdentityException;
import com.digitalecosystem.identityservice.repository.BackupMetadataRepository;
import com.digitalecosystem.identityservice.repository.UserIdentityRepository;
import com.digitalecosystem.identityservice.util.EncryptionUtil;
import com.digitalecosystem.identityservice.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {

    private final UserIdentityRepository userIdentityRepository;
    private final BackupMetadataRepository backupMetadataRepository;
    private final EncryptionUtil encryptionUtil;
    private final HashUtil hashUtil;
    private final ObjectMapper objectMapper;

    /**
     * Create encrypted backup
     */
    @Transactional
    public BackupCreateResponse createBackup(BackupCreateRequest request) {
        UserIdentity userIdentity = userIdentityRepository.findByDid(request.getDid())
                .orElseThrow(() -> new IdentityException("DID not found"));

        try {
            // Create backup payload
            Map<String, Object> backupData = new HashMap<>();
            backupData.put("did", userIdentity.getDid());
            backupData.put("publicKey", userIdentity.getPublicKey());
            backupData.put("version", "1.0");
            backupData.put("timestamp", System.currentTimeMillis());

            String jsonData = objectMapper.writeValueAsString(backupData);

            // Encrypt backup
            String encryptedData = encryptionUtil.encrypt(jsonData,
                    request.getPassphrase() != null ? request.getPassphrase() : request.getDid());

            // Calculate hash
            String fileHash = hashUtil.sha256(encryptedData);

            // Mark previous backups as not latest
            backupMetadataRepository.findByUserIdentityAndIsLatest(userIdentity, true)
                    .ifPresent(backup -> {
                        backup.setIsLatest(false);
                        backupMetadataRepository.save(backup);
                    });

            // Save backup metadata
            BackupMetadata metadata = BackupMetadata.builder()
                    .userIdentity(userIdentity)
                    .backupVersion("1.0")
                    .fileHash(fileHash)
                    .storagePath("local") // In production, this would be S3/MinIO path
                    .isLatest(true)
                    .build();

            backupMetadataRepository.save(metadata);

            log.info("Backup created for DID: {}", request.getDid());

            return BackupCreateResponse.builder()
                    .encryptedFile(encryptedData)
                    .fileHash(fileHash)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create backup", e);
            throw new IdentityException("Failed to create backup", e);
        }
    }

    /**
     * Restore identity from backup
     */
    @Transactional
    public RestoreIdentityResponse restoreIdentity(RestoreIdentityRequest request) {
        try {
            // Decrypt backup
            String decryptedData = encryptionUtil.decrypt(
                    request.getEncryptedFile(),
                    request.getPassphrase() != null ? request.getPassphrase() : ""
            );

            // Parse backup data
            @SuppressWarnings("unchecked")
            Map<String, Object> backupData = objectMapper.readValue(decryptedData, Map.class);

            String did = (String) backupData.get("did");
            String publicKey = (String) backupData.get("publicKey");

            // Check if identity already exists
            Optional<UserIdentity> existing = userIdentityRepository.findByDid(did);

            if (existing.isPresent()) {
                log.info("Identity already exists, updating: {}", did);
                UserIdentity userIdentity = existing.get();
                userIdentity.setLastVerified(LocalDateTime.now());
                userIdentityRepository.save(userIdentity);
            } else {
                log.info("Restoring new identity: {}", did);
                UserIdentity userIdentity = UserIdentity.builder()
                        .did(did)
                        .publicKey(publicKey)
                        .syncStatus("restored")
                        .lastVerified(LocalDateTime.now())
                        .build();
                userIdentityRepository.save(userIdentity);
            }

            return RestoreIdentityResponse.builder()
                    .status("SUCCESS")
                    .did(did)
                    .message("Identity restored successfully")
                    .build();

        } catch (Exception e) {
            log.error("Failed to restore identity", e);
            throw new IdentityException("Failed to restore identity: " + e.getMessage(), e);
        }
    }
}