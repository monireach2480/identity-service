package com.digitalecosystem.identityservice.util;

import com.digitalecosystem.identityservice.exception.EncryptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Component
@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;
    private static final int SALT_SIZE = 16;
    private static final int ITERATION_COUNT = 65536;

    /**
     * Encrypt data using AES-GCM with a password-derived key
     */
    public String encrypt(String data, String passphrase) {
        try {
            byte[] salt = generateSalt();
            SecretKey key = deriveKey(passphrase, salt);
            byte[] iv = generateIV();

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] encryptedData = cipher.doFinal(data.getBytes());

            // Combine salt + iv + encrypted data
            ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + iv.length + encryptedData.length);
            byteBuffer.put(salt);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypt data using AES-GCM with a password-derived key
     */
    public String decrypt(String encryptedData, String passphrase) {
        try {
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);

            // Extract salt, iv, and encrypted data
            byte[] salt = new byte[SALT_SIZE];
            byteBuffer.get(salt);

            byte[] iv = new byte[IV_SIZE];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            SecretKey key = deriveKey(passphrase, salt);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] decryptedData = cipher.doFinal(cipherText);
            return new String(decryptedData);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    /**
     * Derive key from passphrase using PBKDF2
     */
    private SecretKey deriveKey(String passphrase, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    /**
     * Generate random salt
     */
    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_SIZE];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Generate random IV
     */
    private byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Generate AES key
     */
    public SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(KEY_SIZE);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("Failed to generate key", e);
        }
    }
}