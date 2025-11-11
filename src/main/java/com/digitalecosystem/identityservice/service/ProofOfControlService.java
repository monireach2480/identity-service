package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.dto.ChallengeResponse;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.digitalecosystem.identityservice.exception.IdentityException;
import com.digitalecosystem.identityservice.repository.UserIdentityRepository;
import com.digitalecosystem.identityservice.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProofOfControlService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserIdentityRepository userIdentityRepository;
    private final EncryptionUtil encryptionUtil;

    private static final String CHALLENGE_PREFIX = "challenge:";
    private static final int CHALLENGE_TTL = 300; // 5 minutes

    public ChallengeResponse generateChallenge(String did) {
        // Verify DID exists
        UserIdentity userIdentity = userIdentityRepository.findByDid(did)
                .orElseThrow(() -> new IdentityException("DID not found: " + did));

        // Generate random nonce
        byte[] nonceBytes = new byte[32];
        new SecureRandom().nextBytes(nonceBytes);
        String nonce = Base64.getEncoder().encodeToString(nonceBytes);
        String nonceId = java.util.UUID.randomUUID().toString();

        // Store in Redis
        String key = CHALLENGE_PREFIX + nonceId;
        redisTemplate.opsForValue().set(key, did + ":" + nonce, CHALLENGE_TTL, TimeUnit.SECONDS);

        log.info("Generated challenge for DID: {}, nonceId: {}", did, nonceId);

        return ChallengeResponse.builder()
                .nonceId(nonceId)
                .nonce(nonce)
                .ttl(CHALLENGE_TTL)
                .build();
    }

    public boolean verifyProof(String did, String signature, String nonceId) {
        String key = CHALLENGE_PREFIX + nonceId;
        String storedValue = redisTemplate.opsForValue().get(key);

        if (storedValue == null) {
            throw new IdentityException("Challenge expired or not found");
        }

        // Extract DID and nonce from stored value
        String[] parts = storedValue.split(":", 2);
        if (parts.length != 2 || !parts[0].equals(did)) {
            throw new IdentityException("Invalid challenge data");
        }

        String nonce = parts[1];

        // Get user's public key
        UserIdentity userIdentity = userIdentityRepository.findByDid(did)
                .orElseThrow(() -> new IdentityException("DID not found: " + did));

        // TODO: Implement actual signature verification with Ed25519
        // For now, this is a placeholder - you'll need to implement proper crypto verification
        boolean isValid = verifySignature(userIdentity.getPublicKey(), nonce, signature);

        if (isValid) {
            // Clean up challenge
            redisTemplate.delete(key);
            log.info("Proof of control verified for DID: {}", did);
        } else {
            log.warn("Proof of control failed for DID: {}", did);
        }

        return isValid;
    }

    private boolean verifySignature(String publicKey, String data, String signature) {
        // TODO: Implement actual Ed25519 signature verification
        // This is a placeholder - you need to implement proper cryptographic verification
        // using Bouncy Castle or similar library
        log.warn("Signature verification not implemented - returning true for development");
        return true; // Remove this in production
    }
}