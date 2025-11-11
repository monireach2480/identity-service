package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.dto.ChallengeResponse;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.digitalecosystem.identityservice.exception.IdentityException;
import com.digitalecosystem.identityservice.repository.UserIdentityRepository;
import com.digitalecosystem.identityservice.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
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

    // Add Bouncy Castle provider statically
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

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

        // Implement actual signature verification with Ed25519
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

    private boolean verifySignature(String publicKeyBase58, String data, String signatureBase64) {
        try {
            // Decode Base58 public key (commonly used format for Ed25519 in DIDs)
            byte[] publicKeyBytes = decodeBase58(publicKeyBase58);

            // Decode Base64 signature
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            // Get data bytes (the nonce that was signed)
            byte[] dataBytes = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            // Create Ed25519 signature verifier
            Signature verifier = Signature.getInstance("Ed25519", "BC");

            // Convert raw public key bytes to PublicKey object
            PublicKey publicKey = getPublicKeyFromBytes(publicKeyBytes);

            // Initialize verification with public key
            verifier.initVerify(publicKey);

            // Add the data that was signed
            verifier.update(dataBytes);

            // Verify the signature
            boolean isValid = verifier.verify(signatureBytes);

            log.debug("Signature verification result: {} for public key: {}", isValid, publicKeyBase58);
            return isValid;

        } catch (Exception e) {
            log.error("Signature verification failed for public key: {}", publicKeyBase58, e);
            return false; // SAFE DEFAULT - reject on any error
        }
    }

    private PublicKey getPublicKeyFromBytes(byte[] publicKeyBytes) throws Exception {
        try {
            // For Ed25519, we need to create the proper key specification
            // Ed25519 public keys are 32 bytes
            if (publicKeyBytes.length != 32) {
                throw new IdentityException("Invalid Ed25519 public key length: " + publicKeyBytes.length);
            }

            // Create X509 encoded key spec (standard format)
            // For Ed25519, we need to prepend the OID and key bytes
            byte[] encodedKey = encodeEd25519PublicKey(publicKeyBytes);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519", "BC");
            return keyFactory.generatePublic(keySpec);

        } catch (Exception e) {
            log.error("Failed to create public key from bytes", e);
            throw new IdentityException("Invalid public key format");
        }
    }

    private byte[] encodeEd25519PublicKey(byte[] rawPublicKey) {
        // Ed25519 public key encoding for X509 format
        // This creates the proper ASN.1 structure for Ed25519 public keys
        byte[] oid = new byte[] {
                0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21, 0x00
        };
        byte[] result = new byte[oid.length + rawPublicKey.length];
        System.arraycopy(oid, 0, result, 0, oid.length);
        System.arraycopy(rawPublicKey, 0, result, oid.length, rawPublicKey.length);
        return result;
    }

    private byte[] decodeBase58(String base58String) {
        // Base58 decoding implementation (commonly used in DIDs)
        // You might want to use a library like BitcoinJ or create a simple implementation
        try {
            // Simple Base58 decoding (you can replace this with a library)
            char[] input = base58String.toCharArray();
            byte[] result = new byte[input.length * 2]; // Estimate size

            // Basic Base58 decoding logic
            int resultLength = 0;
            for (int i = 0; i < input.length; i++) {
                char c = input[i];
                int digit = base58DigitValue(c);
                if (digit < 0) {
                    throw new IdentityException("Invalid Base58 character: " + c);
                }

                // Carry over the digit
                int carry = digit;
                for (int j = 0; j < resultLength; j++) {
                    carry += (result[j] & 0xFF) * 58;
                    result[j] = (byte) (carry & 0xFF);
                    carry >>= 8;
                }

                while (carry > 0) {
                    result[resultLength++] = (byte) (carry & 0xFF);
                    carry >>= 8;
                }
            }

            // Trim to actual size
            byte[] trimmedResult = new byte[resultLength];
            System.arraycopy(result, 0, trimmedResult, 0, resultLength);
            return trimmedResult;

        } catch (Exception e) {
            log.error("Base58 decoding failed for string: {}", base58String, e);
            throw new IdentityException("Invalid Base58 encoding");
        }
    }

    private int base58DigitValue(char c) {
        if (c >= '1' && c <= '9') return c - '1';
        if (c >= 'A' && c <= 'H') return c - 'A' + 9;
        if (c >= 'J' && c <= 'N') return c - 'J' + 17;
        if (c >= 'P' && c <= 'Z') return c - 'P' + 22;
        if (c >= 'a' && c <= 'k') return c - 'a' + 33;
        if (c >= 'm' && c <= 'z') return c - 'm' + 44;
        return -1; // Invalid character
    }
}