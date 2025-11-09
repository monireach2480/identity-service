package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.dto.SyncBatchRequest;
import com.digitalecosystem.identityservice.dto.SyncBatchResponse;
import com.digitalecosystem.identityservice.entity.SyncQueue;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.digitalecosystem.identityservice.repository.SyncQueueRepository;
import com.digitalecosystem.identityservice.repository.UserIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final SyncQueueRepository syncQueueRepository;
    private final UserIdentityRepository userIdentityRepository;

    /**
     * Process batch sync operations
     */
    @Transactional
    public SyncBatchResponse processBatchSync(SyncBatchRequest request) {
        List<SyncBatchResponse.ConflictInfo> conflicts = new ArrayList<>();
        int syncedCount = 0;

        for (SyncBatchRequest.SyncOperation operation : request.getOperations()) {
            try {
                processOperation(operation);
                syncedCount++;
            } catch (Exception e) {
                log.error("Failed to process operation: {}", operation.getOperation(), e);
                conflicts.add(SyncBatchResponse.ConflictInfo.builder()
                        .operation(operation.getOperation())
                        .reason(e.getMessage())
                        .clientData(operation.getData())
                        .build());
            }
        }

        return SyncBatchResponse.builder()
                .synced(syncedCount)
                .conflicts(conflicts)
                .serverTime(System.currentTimeMillis())
                .build();
    }

    private void processOperation(SyncBatchRequest.SyncOperation operation) {
        switch (operation.getOperation().toLowerCase()) {
            case "create":
                handleCreate(operation);
                break;
            case "update":
                handleUpdate(operation);
                break;
            case "delete":
                handleDelete(operation);
                break;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation.getOperation());
        }
    }

    private void handleCreate(SyncBatchRequest.SyncOperation operation) {
        String did = (String) operation.getData().get("did");
        String publicKey = (String) operation.getData().get("publicKey");

        if (!userIdentityRepository.existsByDid(did)) {
            UserIdentity userIdentity = UserIdentity.builder()
                    .did(did)
                    .publicKey(publicKey)
                    .syncStatus("synced")
                    .build();
            userIdentityRepository.save(userIdentity);
            log.info("Created identity via sync: {}", did);
        }
    }

    private void handleUpdate(SyncBatchRequest.SyncOperation operation) {
        String did = (String) operation.getData().get("did");
        UserIdentity userIdentity = userIdentityRepository.findByDid(did)
                .orElseThrow(() -> new IllegalArgumentException("DID not found: " + did));

        userIdentity.setUpdatedAt(LocalDateTime.now());
        userIdentity.setSyncStatus("synced");
        userIdentityRepository.save(userIdentity);
        log.info("Updated identity via sync: {}", did);
    }

    private void handleDelete(SyncBatchRequest.SyncOperation operation) {
        String did = (String) operation.getData().get("did");
        userIdentityRepository.findByDid(did).ifPresent(userIdentity -> {
            userIdentityRepository.delete(userIdentity);
            log.info("Deleted identity via sync: {}", did);
        });
    }
}