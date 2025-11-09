package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.dto.*;
import com.digitalecosystem.identityservice.entity.UserContact;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.digitalecosystem.identityservice.exception.IdentityException;
import com.digitalecosystem.identityservice.repository.UserContactRepository;
import com.digitalecosystem.identityservice.repository.UserIdentityRepository;
import com.digitalecosystem.identityservice.util.DIDUtil;
import com.digitalecosystem.identityservice.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityService {

    private final UserIdentityRepository userIdentityRepository;
    private final UserContactRepository userContactRepository;
    private final DIDUtil didUtil;
    private final HashUtil hashUtil;

    /**
     * Check if identity exists
     */
    public IdentityCheckResponse checkIdentity(String identifier) {
        Optional<UserContact> contact = userContactRepository
                .findByEmailOrPhoneNumber(identifier, identifier);

        if (contact.isPresent() && contact.get().getUserIdentity() != null) {
            return IdentityCheckResponse.builder()
                    .exists(true)
                    .did(contact.get().getUserIdentity().getDid())
                    .build();
        }

        return IdentityCheckResponse.builder()
                .exists(false)
                .build();
    }

    /**
     * Create DID and register identity
     */
    @Transactional
    public DIDCreateResponse createDID(DIDCreateRequest request) {
        // Validate DID format
        if (!didUtil.isValidDID(request.getDid())) {
            throw new IdentityException("Invalid DID format");
        }

        // Check if DID already exists
        if (userIdentityRepository.existsByDid(request.getDid())) {
            throw new IdentityException("DID already exists");
        }

        // Create user identity
        UserIdentity userIdentity = UserIdentity.builder()
                .did(request.getDid())
                .publicKey(request.getPublicKey())
                .syncStatus(request.getOfflineCreated() ? "pending_sync" : "synced")
                .lastVerified(LocalDateTime.now())
                .build();

        userIdentity = userIdentityRepository.save(userIdentity);

        log.info("DID created successfully: {}", request.getDid());

        return DIDCreateResponse.builder()
                .did(userIdentity.getDid())
                .status("registered")
                .timestamp(userIdentity.getCreatedAt())
                .build();
    }

    /**
     * Link contact to identity
     */
    @Transactional
    public void linkContact(String did, String identifier, boolean isEmail) {
        UserIdentity userIdentity = userIdentityRepository.findByDid(did)
                .orElseThrow(() -> new IdentityException("DID not found"));

        UserContact contact = UserContact.builder()
                .userIdentity(userIdentity)
                .email(isEmail ? identifier : null)
                .phoneNumber(isEmail ? null : identifier)
                .isVerified(true)
                .build();

        userContactRepository.save(contact);
        log.info("Contact linked to DID: {}", did);
    }
}