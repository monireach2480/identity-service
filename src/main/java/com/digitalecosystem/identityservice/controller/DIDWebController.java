package com.digitalecosystem.identityservice.controller;

import com.digitalecosystem.identityservice.dto.*;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.digitalecosystem.identityservice.exception.IdentityException;
import com.digitalecosystem.identityservice.repository.UserIdentityRepository;
import com.digitalecosystem.identityservice.service.DIDWebPublisherService;
import com.digitalecosystem.identityservice.service.ProofOfControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/did")
@RequiredArgsConstructor
@Slf4j
public class DIDWebController {

    private final ProofOfControlService proofOfControlService;
    private final DIDWebPublisherService didWebPublisherService;
    private final UserIdentityRepository userIdentityRepository;

    /**
     * Generate proof-of-control challenge
     */
    @GetMapping("/challenge")
    public ResponseEntity<ChallengeResponse> generateChallenge(@RequestParam String did) {
        log.info("Generating challenge for DID: {}", did);
        ChallengeResponse response = proofOfControlService.generateChallenge(did);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify proof of control
     */
    @PostMapping("/prove-control")
    public ResponseEntity<Map<String, Object>> proveControl(@Valid @RequestBody ProofOfControlRequest request) {
        log.info("Proof of control verification for DID: {}", request.getDid());

        boolean verified = proofOfControlService.verifyProof(
                request.getDid(),
                request.getSignature(),
                request.getNonceId()
        );

        if (!verified) {
            throw new IdentityException("Proof of control verification failed");
        }

        return ResponseEntity.ok(Map.of(
                "verified", true,
                "message", "Proof of control verified successfully"
        ));
    }

    /**
     * Publish DID to did:web
     */
    @PostMapping("/publish")
    public ResponseEntity<DIDPublishResponse> publishDID(@Valid @RequestBody DIDPublishRequest request) {
        log.info("Publish request for DID: {}, domain: {}", request.getDid(), request.getDomain());

        String publicDid = didWebPublisherService.publishDID(
                request.getDid(),
                request.getDomain(),
                request.getPath()
        );

        return ResponseEntity.ok(DIDPublishResponse.builder()
                .status("published")
                .publicDid(publicDid)
                .message("DID published successfully")
                .didWebStatus("published")
                .build());
    }

    /**
     * Unpublish/revoke did:web
     */
    @PostMapping("/unpublish")
    public ResponseEntity<DIDPublishResponse> unpublishDID(@RequestBody Map<String, String> request) {
        String did = request.get("did");
        String reason = request.get("reason");

        log.info("Unpublish request for DID: {}, reason: {}", did, reason);

        didWebPublisherService.unpublishDID(did, reason);

        return ResponseEntity.ok(DIDPublishResponse.builder()
                .status("revoked")
                .message("DID unpublished successfully")
                .didWebStatus("revoked")
                .build());
    }

    /**
     * Check DID publish status
     */
    @GetMapping("/status")
    public ResponseEntity<DIDStatusResponse> getDIDStatus(@RequestParam String did) {
        UserIdentity userIdentity = userIdentityRepository.findByDid(did)
                .orElseThrow(() -> new IdentityException("DID not found: " + did));

        return ResponseEntity.ok(DIDStatusResponse.builder()
                .did(userIdentity.getDid())
                .publicDid(userIdentity.getPublicDid())
                .didWebStatus(userIdentity.getDidWebStatus())
                .publishedAt(userIdentity.getDidWebPublishedAt())
                .metadata(userIdentity.getDidWebMetadata())
                .build());
    }
}