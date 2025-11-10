package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.dto.*;
import com.digitalecosystem.identityservice.entity.UserContact;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.digitalecosystem.identityservice.exception.IdentityException;
import com.digitalecosystem.identityservice.exception.IdentityExistsException;
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
     * Check if identity exists with identifier hashing (REQUIRED by spec)
     */
    public IdentityCheckResponse checkIdentity(String identifier) {
        String identifierHash = hashUtil.sha256(identifier);
        log.debug("Checking identity with hash: {} for identifier: {}", identifierHash, identifier);

        // Check by hash first (REQUIRED by spec)
        Optional<UserContact> contact = userContactRepository.findByIdentifierHash(identifierHash);

        // Fallback to raw identifier for existing data
        if (contact.isEmpty()) {
            contact = userContactRepository.findByEmailOrPhoneNumber(identifier, identifier);

            // If found by raw identifier, update with hash
            if (contact.isPresent()) {
                UserContact userContact = contact.get();
                userContact.setIdentifierHash(identifierHash);
                userContactRepository.save(userContact);
                log.info("Updated existing contact with identifier hash: {}", identifierHash);
            }
        }

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
     * Create DID and register identity with 409 error (REQUIRED by spec)
     */
    @Transactional
    public DIDCreateResponse createDID(DIDCreateRequest request) {
        // Validate DID format
        if (!didUtil.isValidDID(request.getDid())) {
            throw new IdentityException("Invalid DID format");
        }

        // Check if DID already exists - throw 409 (REQUIRED by spec)
        if (userIdentityRepository.existsByDid(request.getDid())) {
            throw new IdentityExistsException("DID already exists: " + request.getDid());
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
     * Link contact to identity with identifier hashing (REQUIRED by spec)
     */
    @Transactional
    public void linkContact(String did, String identifier, boolean isEmail) {
        UserIdentity userIdentity = userIdentityRepository.findByDid(did)
                .orElseThrow(() -> new IdentityException("DID not found"));

        String identifierHash = hashUtil.sha256(identifier);

        UserContact contact = UserContact.builder()
                .userIdentity(userIdentity)
                .email(isEmail ? identifier : null)
                .phoneNumber(isEmail ? null : identifier)
                .identifierHash(identifierHash) // STORE HASH (REQUIRED by spec)
                .isVerified(true)
                .build();

        userContactRepository.save(contact);
        log.info("Contact linked to DID: {} with identifier hash: {}", did, identifierHash);
    }
}