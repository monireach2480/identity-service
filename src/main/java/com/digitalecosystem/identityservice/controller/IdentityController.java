package com.digitalecosystem.identityservice.controller;

import com.digitalecosystem.identityservice.dto.*;
import com.digitalecosystem.identityservice.service.BackupService;
import com.digitalecosystem.identityservice.service.IdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/identity")
@RequiredArgsConstructor
@Slf4j
public class IdentityController {

    private final IdentityService identityService;
    private final BackupService backupService;

    /**
     * Check if identity exists
     * POST /api/v1/identity/check
     */
    @PostMapping("/check")
    public ResponseEntity<IdentityCheckResponse> checkIdentity(@Valid @RequestBody IdentityCheckRequest request) {
        log.info("Identity check request for: {}", request.getIdentifier());

        IdentityCheckResponse response = identityService.checkIdentity(request.getIdentifier());
        return ResponseEntity.ok(response);
    }

    /**
     * Register DID
     * POST /api/v1/identity/register
     */
    @PostMapping("/register")
    public ResponseEntity<DIDCreateResponse> registerDID(@Valid @RequestBody DIDCreateRequest request) {
        log.info("DID registration request: {}", request.getDid());

        DIDCreateResponse response = identityService.createDID(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Create backup
     * POST /api/v1/identity/backup
     */
    @PostMapping("/backup")
    public ResponseEntity<BackupCreateResponse> createBackup(@Valid @RequestBody BackupCreateRequest request) {
        log.info("Backup creation request for DID: {}", request.getDid());

        BackupCreateResponse response = backupService.createBackup(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Restore identity
     * POST /api/v1/identity/restore
     */
    @PostMapping("/restore")
    public ResponseEntity<RestoreIdentityResponse> restoreIdentity(@Valid @RequestBody RestoreIdentityRequest request) {
        log.info("Identity restore request");

        RestoreIdentityResponse response = backupService.restoreIdentity(request);
        return ResponseEntity.ok(response);
    }
}