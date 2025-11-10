package com.digitalecosystem.identityservice.controller;

import com.digitalecosystem.identityservice.dto.TrustTokenRequest;
import com.digitalecosystem.identityservice.dto.TrustTokenResponse;
import com.digitalecosystem.identityservice.service.TrustTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trust-token")
@RequiredArgsConstructor
@Slf4j
public class TrustTokenController {

    private final TrustTokenService trustTokenService;

    @PostMapping("/setup")
    public ResponseEntity<TrustTokenResponse> setupTrustToken(
            @Valid @RequestBody TrustTokenRequest request) {
        log.info("Trust token setup request for DID: {}", request.getDid());

        TrustTokenResponse response = trustTokenService.setupTrustToken(request);
        return ResponseEntity.ok(response);
    }
}