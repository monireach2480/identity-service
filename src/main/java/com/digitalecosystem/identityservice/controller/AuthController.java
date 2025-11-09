package com.digitalecosystem.identityservice.controller;

import com.digitalecosystem.identityservice.dto.*;
import com.digitalecosystem.identityservice.exception.OTPException;
import com.digitalecosystem.identityservice.service.IdentityService;
import com.digitalecosystem.identityservice.service.OTPService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final OTPService otpService;
    private final IdentityService identityService;

    /**
     * Generate OTP for registration
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<OTPGenerateResponse> register(@Valid @RequestBody OTPGenerateRequest request) {
        log.info("Registration request for: {}", request.getIdentifier());

        // Check if identity already exists
        IdentityCheckResponse check = identityService.checkIdentity(request.getIdentifier());
        if (check.getExists()) {
            return ResponseEntity.badRequest().body(
                    OTPGenerateResponse.builder()
                            .message("Identity already exists. Please use restore flow.")
                            .build()
            );
        }

        // Generate and send OTP
        String otpId = otpService.generateOTP(request.getIdentifier(), request.getChannel());

        return ResponseEntity.ok(OTPGenerateResponse.builder()
                .message("OTP sent successfully")
                .expiresIn(180)
                .otpId(otpId)
                .build());
    }

    /**
     * Verify OTP
     * POST /api/v1/auth/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<OTPValidateResponse> verifyOTP(@Valid @RequestBody OTPValidateRequest request) {
        log.info("OTP verification request for: {}", request.getIdentifier());

        boolean isValid = otpService.validateOTP(request.getIdentifier(), request.getOtp());

        if (!isValid) {
            throw new OTPException("Invalid or expired OTP");
        }

        return ResponseEntity.ok(OTPValidateResponse.builder()
                .status("VALID")
                .message("OTP verified successfully")
                .build());
    }

    /**
     * Resend OTP
     * POST /api/v1/auth/resend-otp
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<OTPGenerateResponse> resendOTP(@Valid @RequestBody OTPGenerateRequest request) {
        log.info("Resend OTP request for: {}", request.getIdentifier());

        String otpId = otpService.generateOTP(request.getIdentifier(), request.getChannel());

        return ResponseEntity.ok(OTPGenerateResponse.builder()
                .message("OTP resent successfully")
                .expiresIn(180)
                .otpId(otpId)
                .build());
    }
}