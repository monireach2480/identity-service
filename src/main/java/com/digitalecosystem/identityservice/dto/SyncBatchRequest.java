package com.digitalecosystem.identityservice.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SyncBatchRequest {
    private List<SyncOperation> operations;

    @Data
    public static class SyncOperation {
        private String operation; // create, update, delete
        private Map<String, Object> data;
        private Long timestamp;
    }
}