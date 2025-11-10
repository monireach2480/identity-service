package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.exception.OTPException;
import com.digitalecosystem.identityservice.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPService {

    private final RedisTemplate<String, String> redisTemplate;
    private final HashUtil hashUtil;
    private final EmailService emailService;
    private final SMSService smsService;

    @Value("${app.otp.expiry-seconds}")
    private Integer otpExpirySeconds;

    @Value("${app.otp.max-attempts}")
    private Integer maxAttempts;

    private static final String OTP_PREFIX = "otp:";
    private static final String ATTEMPT_PREFIX = "otp_attempt:";
    private static final String VERIFIED_PREFIX = "verified:";

    /**
     * Generate and send OTP
     */
    public String generateOTP(String identifier, String channel) {
        // Check rate limiting
        String attemptKey = ATTEMPT_PREFIX + identifier;
        String attempts = redisTemplate.opsForValue().get(attemptKey);

        if (attempts != null && Integer.parseInt(attempts) >= maxAttempts) {
            throw new OTPException("Maximum OTP attempts exceeded. Please try again later.");
        }

        // Generate 6-digit OTP
        String otp = generateRandomOTP();
        String otpHash = hashUtil.sha256(otp);

        // Store in Redis
        String key = OTP_PREFIX + identifier;
        redisTemplate.opsForValue().set(key, otpHash, otpExpirySeconds, TimeUnit.SECONDS);

        // Increment attempt counter
        redisTemplate.opsForValue().increment(attemptKey);
        redisTemplate.expire(attemptKey, otpExpirySeconds, TimeUnit.SECONDS);

        // Send OTP
        if ("email".equalsIgnoreCase(channel)) {
            emailService.sendOTP(identifier, otp);
        } else if ("phone".equalsIgnoreCase(channel)) {
            smsService.sendOTP(identifier, otp);
        } else {
            throw new OTPException("Invalid channel: " + channel);
        }

        log.info("OTP generated for identifier: {}", identifier);
        return key;
    }

    /**
     * Validate OTP
     */
    public boolean validateOTP(String identifier, String otp) {
        String key = OTP_PREFIX + identifier;
        String storedHash = redisTemplate.opsForValue().get(key);

        if (storedHash == null) {
            throw new OTPException("OTP expired or not found");
        }

        String inputHash = hashUtil.sha256(otp);
        boolean isValid = storedHash.equals(inputHash);

        if (isValid) {
            // Delete OTP after successful validation
            redisTemplate.delete(key);
            redisTemplate.delete(ATTEMPT_PREFIX + identifier);
            log.info("OTP validated successfully for: {}", identifier);
        } else {
            log.warn("Invalid OTP attempt for: {}", identifier);
        }

        return isValid;
    }

    /**
     * Mark identifier as verified after OTP validation
     */
    public void markIdentifierAsVerified(String identifier) {
        String key = VERIFIED_PREFIX + identifier;
        redisTemplate.opsForValue().set(key, "true", 1800, TimeUnit.SECONDS); // 30 minutes
        log.info("Identifier marked as verified: {}", identifier);
    }

    /**
     * Check if identifier is verified
     */
    public boolean isIdentifierVerified(String identifier) {
        String key = VERIFIED_PREFIX + identifier;
        return "true".equals(redisTemplate.opsForValue().get(key));
    }

    /**
     * Invalidate verified identifier (clean up after use)
     */
    public void invalidateVerifiedIdentifier(String identifier) {
        String key = VERIFIED_PREFIX + identifier;
        redisTemplate.delete(key);
        log.info("Verified identifier invalidated: {}", identifier);
    }

    /**
     * Generate random 6-digit OTP
     */
    private String generateRandomOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}