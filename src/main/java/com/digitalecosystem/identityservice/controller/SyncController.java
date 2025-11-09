package com.digitalecosystem.identityservice.controller;

import com.digitalecosystem.identityservice.dto.SyncBatchRequest;
import com.digitalecosystem.identityservice.dto.SyncBatchResponse;
import com.digitalecosystem.identityservice.service.SyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncController {

    private final SyncService syncService;

    /**
     * Check sync availability
     * GET /api/v1/sync/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "available");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    /**
     * Batch sync operations
     * POST /api/v1/sync/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<SyncBatchResponse> batchSync(@Valid @RequestBody SyncBatchRequest request) {
        log.info("Batch sync request with {} operations", request.getOperations().size());

        SyncBatchResponse response = syncService.processBatchSync(request);
        return ResponseEntity.ok(response);
    }
}