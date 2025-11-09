package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SyncBatchResponse {
    private Integer synced;
    private List<ConflictInfo> conflicts;
    private Long serverTime;

    @Data
    @Builder
    public static class ConflictInfo {
        private String operation;
        private String reason;
        private Object serverData;
        private Object clientData;
    }
}