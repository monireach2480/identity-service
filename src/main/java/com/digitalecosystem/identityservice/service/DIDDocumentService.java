package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DIDDocumentService {

    private final ObjectMapper objectMapper;

    public String generateDIDDocument(UserIdentity userIdentity, String domain, String path) {
        String didWeb = "did:web:" + domain + ":" + path;

        ObjectNode didDocument = objectMapper.createObjectNode();

        // @context
        didDocument.put("@context", "https://www.w3.org/ns/did/v1");

        // id
        didDocument.put("id", didWeb);

        // verificationMethod
        ObjectNode verificationMethod = objectMapper.createObjectNode();
        verificationMethod.put("id", didWeb + "#key-1");
        verificationMethod.put("type", "Ed25519VerificationKey2018");
        verificationMethod.put("controller", didWeb);
        verificationMethod.put("publicKeyBase58", userIdentity.getPublicKey());

        didDocument.set("verificationMethod", objectMapper.createArrayNode().add(verificationMethod));

        // authentication
        didDocument.set("authentication", objectMapper.createArrayNode().add(didWeb + "#key-1"));

        // assertionMethod
        didDocument.set("assertionMethod", objectMapper.createArrayNode().add(didWeb + "#key-1"));

        // created
        didDocument.put("created", Instant.now().toString());

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(didDocument);
        } catch (Exception e) {
            log.error("Failed to generate DID document", e);
            throw new RuntimeException("Failed to generate DID document", e);
        }
    }

    public String generateDeactivatedDIDDocument(String didWeb) {
        ObjectNode didDocument = objectMapper.createObjectNode();
        didDocument.put("@context", "https://www.w3.org/ns/did/v1");
        didDocument.put("id", didWeb);
        didDocument.put("deactivated", true);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(didDocument);
        } catch (Exception e) {
            log.error("Failed to generate deactivated DID document", e);
            throw new RuntimeException("Failed to generate deactivated DID document", e);
        }
    }
}