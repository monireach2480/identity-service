package com.digitalecosystem.identityservice.controller;

import com.digitalecosystem.identityservice.dto.*;
import com.digitalecosystem.identityservice.exception.IdentityException;
import com.digitalecosystem.identityservice.service.BackupService;
import com.digitalecosystem.identityservice.service.IdentityService;
import com.digitalecosystem.identityservice.service.OTPService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/identity")
@RequiredArgsConstructor
@Slf4j
public class IdentityController {

    private final IdentityService identityService;
    private final BackupService backupService;
    private final OTPService otpService;

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
     * Register DID with verified identifier linking
     * POST /api/v1/identity/register
     */
    @PostMapping("/register")
    public ResponseEntity<DIDCreateResponse> registerDID(@Valid @RequestBody DIDCreateRequest request) {
        log.info("DID registration request: {}", request.getDid());

        // Check if identifier was verified via OTP
        if (request.getVerifiedIdentifier() != null && !otpService.isIdentifierVerified(request.getVerifiedIdentifier())) {
            throw new IdentityException("Identifier not verified. Please complete OTP verification first.");
        }

        DIDCreateResponse response = identityService.createDID(request);

        // Link contact if identifier was provided and verified
        if (request.getVerifiedIdentifier() != null && otpService.isIdentifierVerified(request.getVerifiedIdentifier())) {
            boolean isEmail = request.getVerifiedIdentifier().contains("@");
            identityService.linkContact(request.getDid(), request.getVerifiedIdentifier(), isEmail);
            otpService.invalidateVerifiedIdentifier(request.getVerifiedIdentifier()); // Clean up
            log.info("Auto-linked verified identifier: {} to DID: {}", request.getVerifiedIdentifier(), request.getDid());
        }

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

    /**
     * Test endpoint to manually link contact (for testing)
     * POST /api/v1/identity/test-link
     */
    @PostMapping("/test-link")
    public ResponseEntity<Map<String, Object>> testLinkContact(@RequestBody Map<String, String> request) {
        String did = request.get("did");
        String identifier = request.get("identifier");

        log.info("Manual contact link test: {} -> {}", identifier, did);

        boolean isEmail = identifier.contains("@");
        identityService.linkContact(did, identifier, isEmail);

        return ResponseEntity.ok(Map.of(
                "message", "Contact linked successfully",
                "did", did,
                "identifier", identifier
        ));
    }
}