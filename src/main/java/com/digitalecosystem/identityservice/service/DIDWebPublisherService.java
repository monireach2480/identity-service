package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.entity.DIDWebAuditLog;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.digitalecosystem.identityservice.exception.IdentityException;
import com.digitalecosystem.identityservice.repository.DIDWebAuditLogRepository;
import com.digitalecosystem.identityservice.repository.UserIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DIDWebPublisherService {

    private final UserIdentityRepository userIdentityRepository;
    private final DIDWebAuditLogRepository auditLogRepository;
    private final DIDDocumentService didDocumentService;

    @Transactional
    public String publishDID(String did, String domain, String path) {
        UserIdentity userIdentity = userIdentityRepository.findByDid(did)
                .orElseThrow(() -> new IdentityException("DID not found: " + did));

        // Generate path if not provided
        if (path == null || path.trim().isEmpty()) {
            path = "users:" + userIdentity.getId();
        }

        String publicDid = "did:web:" + domain + ":" + path;
        String didDocument = didDocumentService.generateDIDDocument(userIdentity, domain, path);

        // Update user identity
        userIdentity.setPublicDid(publicDid);
        userIdentity.setDidWebPath(path);
        userIdentity.setDidWebStatus("published");
        userIdentity.setDidWebPublishedAt(LocalDateTime.now());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("domain", domain);
        metadata.put("path", path);
        metadata.put("documentHash", didDocument.hashCode());
        metadata.put("publishedAt", LocalDateTime.now().toString());
        userIdentity.setDidWebMetadata(metadata);

        userIdentityRepository.save(userIdentity);

        // Publish to web hosting (implementation depends on your hosting strategy)
        publishToWebHost(publicDid, didDocument);

        // Audit log
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("domain", domain);
        auditDetails.put("path", path);
        auditDetails.put("documentSize", didDocument.length());

        DIDWebAuditLog auditLog = DIDWebAuditLog.builder()
                .userIdentity(userIdentity)
                .operation("PUBLISH")
                .publicDid(publicDid)
                .status("SUCCESS")
                .details(auditDetails)
                .build();
        auditLogRepository.save(auditLog);

        log.info("DID published successfully: {}", publicDid);
        return publicDid;
    }

    @Transactional
    public void unpublishDID(String did, String reason) {
        UserIdentity userIdentity = userIdentityRepository.findByDid(did)
                .orElseThrow(() -> new IdentityException("DID not found: " + did));

        if (!"published".equals(userIdentity.getDidWebStatus())) {
            throw new IdentityException("DID is not published: " + did);
        }

        String publicDid = userIdentity.getPublicDid();
        String deactivatedDocument = didDocumentService.generateDeactivatedDIDDocument(publicDid);

        // Update user identity
        userIdentity.setDidWebStatus("revoked");
        userIdentity.setDidWebMetadata(Map.of(
                "revokedAt", LocalDateTime.now().toString(),
                "reason", reason
        ));
        userIdentityRepository.save(userIdentity);

        // Publish deactivated document
        publishToWebHost(publicDid, deactivatedDocument);

        // Audit log
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("reason", reason);
        auditDetails.put("publicDid", publicDid);

        DIDWebAuditLog auditLog = DIDWebAuditLog.builder()
                .userIdentity(userIdentity)
                .operation("UNPUBLISH")
                .publicDid(publicDid)
                .status("SUCCESS")
                .details(auditDetails)
                .build();
        auditLogRepository.save(auditLog);

        log.info("DID unpublished successfully: {}", publicDid);
    }

    private void publishToWebHost(String publicDid, String didDocument) {
        // TODO: Implement based on your hosting strategy
        // Option A: Upload to S3/MinIO
        // Option B: Write to static file directory
        // Option C: Store in database for dynamic resolution

        log.info("Publishing DID document for: {}", publicDid);
        log.debug("DID Document: {}", didDocument);

        // For now, just log - you'll implement the actual publishing mechanism
        // based on your infrastructure choice
    }
}