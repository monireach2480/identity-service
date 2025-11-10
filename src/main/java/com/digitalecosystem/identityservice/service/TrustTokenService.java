package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.dto.TrustTokenRequest;
import com.digitalecosystem.identityservice.dto.TrustTokenResponse;
import com.digitalecosystem.identityservice.entity.DeviceLink;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.digitalecosystem.identityservice.exception.IdentityException;
import com.digitalecosystem.identityservice.repository.DeviceLinkRepository;
import com.digitalecosystem.identityservice.repository.UserIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrustTokenService {

    private final UserIdentityRepository userIdentityRepository;
    private final DeviceLinkRepository deviceLinkRepository;

    @Transactional
    public TrustTokenResponse setupTrustToken(TrustTokenRequest request) {
        // Validate DID exists (REQUIRED)
        UserIdentity userIdentity = userIdentityRepository.findByDid(request.getDid())
                .orElseThrow(() -> new IdentityException("DID not found: " + request.getDid()));

        // Extract device ID (REQUIRED for device binding)
        String deviceIdStr = extractDeviceId(request.getDeviceInfo());
        UUID deviceId = UUID.fromString(deviceIdStr);

        // Create or update device link (REQUIRED)
        DeviceLink deviceLink = deviceLinkRepository.findByDeviceId(deviceId)
                .orElse(DeviceLink.builder()
                        .userIdentity(userIdentity)
                        .deviceId(deviceId)
                        .deviceInfo(request.getDeviceInfo())
                        .status("active")
                        .build());

        deviceLink.setLastLogin(LocalDateTime.now());
        deviceLinkRepository.save(deviceLink);

        log.info("Trust token setup for DID: {} device: {}", request.getDid(), deviceId);

        return TrustTokenResponse.builder()
                .status("ACTIVE")
                .message("Device linked successfully")
                .build();
    }

    private String extractDeviceId(Map<String, Object> deviceInfo) {
        if (deviceInfo == null || !deviceInfo.containsKey("deviceId")) {
            throw new IdentityException("Device ID is required in device info");
        }

        Object deviceIdObj = deviceInfo.get("deviceId");
        if (!(deviceIdObj instanceof String)) {
            throw new IdentityException("Device ID must be a string");
        }

        return (String) deviceIdObj;
    }
}