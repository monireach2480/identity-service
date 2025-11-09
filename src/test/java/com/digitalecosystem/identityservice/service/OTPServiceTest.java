package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.exception.OTPException;
import com.digitalecosystem.identityservice.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OTPServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private HashUtil hashUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private SMSService smsService;

    @InjectMocks
    private OTPService otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "otpExpirySeconds", 180);
        ReflectionTestUtils.setField(otpService, "maxAttempts", 3);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateOTP_Success() {
        // Arrange
        String identifier = "test@example.com";
        String channel = "email";

        when(valueOperations.get(anyString())).thenReturn(null);
        when(hashUtil.sha256(anyString())).thenReturn("hashed_otp");
        doNothing().when(emailService).sendOTP(anyString(), anyString());

        // Act
        String result = otpService.generateOTP(identifier, channel);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("otp:"));
        verify(emailService, times(1)).sendOTP(eq(identifier), anyString());
    }

    @Test
    void generateOTP_MaxAttemptsExceeded() {
        // Arrange
        String identifier = "test@example.com";
        String channel = "email";

        when(valueOperations.get("otp_attempt:" + identifier)).thenReturn("3");

        // Act & Assert
        assertThrows(OTPException.class, () ->
                otpService.generateOTP(identifier, channel));
    }

    @Test
    void validateOTP_Success() {
        // Arrange
        String identifier = "test@example.com";
        String otp = "123456";
        String hashedOTP = "hashed_otp";

        when(valueOperations.get("otp:" + identifier)).thenReturn(hashedOTP);
        when(hashUtil.sha256(otp)).thenReturn(hashedOTP);

        // Act
        boolean result = otpService.validateOTP(identifier, otp);

        // Assert
        assertTrue(result);
        verify(redisTemplate, times(1)).delete("otp:" + identifier);
    }

    @Test
    void validateOTP_Invalid() {
        // Arrange
        String identifier = "test@example.com";
        String otp = "123456";

        when(valueOperations.get("otp:" + identifier)).thenReturn("wrong_hash");
        when(hashUtil.sha256(otp)).thenReturn("correct_hash");

        // Act
        boolean result = otpService.validateOTP(identifier, otp);

        // Assert
        assertFalse(result);
    }
}