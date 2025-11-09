package com.digitalecosystem.identityservice.util;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class DIDUtil {

    private static final Pattern DID_PATTERN = Pattern.compile("^did:[a-z0-9]+:[a-zA-Z0-9._-]+$");

    /**
     * Validate DID format
     */
    public boolean isValidDID(String did) {
        if (did == null || did.isEmpty()) {
            return false;
        }
        return DID_PATTERN.matcher(did).matches();
    }

    /**
     * Extract method from DID
     * Example: did:key:z6Mk... -> key
     */
    public String extractMethod(String did) {
        if (!isValidDID(did)) {
            throw new IllegalArgumentException("Invalid DID format");
        }
        String[] parts = did.split(":");
        return parts.length >= 2 ? parts[1] : null;
    }

    /**
     * Extract identifier from DID
     * Example: did:key:z6Mk... -> z6Mk...
     */
    public String extractIdentifier(String did) {
        if (!isValidDID(did)) {
            throw new IllegalArgumentException("Invalid DID format");
        }
        String[] parts = did.split(":");
        return parts.length >= 3 ? parts[2] : null;
    }
}