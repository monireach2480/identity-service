package com.digitalecosystem.identityservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeResponse {
    private String nonceId;
    private String nonce;
    private Integer ttl;
}