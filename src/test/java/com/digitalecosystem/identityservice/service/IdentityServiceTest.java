package com.digitalecosystem.identityservice.service;

import com.digitalecosystem.identityservice.dto.DIDCreateRequest;
import com.digitalecosystem.identityservice.dto.DIDCreateResponse;
import com.digitalecosystem.identityservice.dto.IdentityCheckResponse;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import com.digitalecosystem.identityservice.exception.IdentityException;
import com.digitalecosystem.identityservice.repository.UserContactRepository;
import com.digitalecosystem.identityservice.repository.UserIdentityRepository;
import com.digitalecosystem.identityservice.util.DIDUtil;
import com.digitalecosystem.identityservice.util.HashUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock
    private UserIdentityRepository userIdentityRepository;

    @Mock
    private UserContactRepository userContactRepository;

    @Mock
    private DIDUtil didUtil;

    @Mock
    private HashUtil hashUtil;

    @InjectMocks
    private IdentityService identityService;

    @Test
    void checkIdentity_NotExists() {
        // Arrange
        when(userContactRepository.findByEmailOrPhoneNumber(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act
        IdentityCheckResponse response = identityService.checkIdentity("test@example.com");

        // Assert
        assertFalse(response.getExists());
        assertNull(response.getDid());
    }

    @Test
    void createDID_Success() {
        // Arrange
        DIDCreateRequest request = new DIDCreateRequest();
        request.setDid("did:key:z6Mkexample");
        request.setPublicKey("publicKeyBase58");
        request.setOfflineCreated(false);

        when(didUtil.isValidDID(anyString())).thenReturn(true);
        when(userIdentityRepository.existsByDid(anyString())).thenReturn(false);
        when(userIdentityRepository.save(any(UserIdentity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DIDCreateResponse response = identityService.createDID(request);

        // Assert
        assertNotNull(response);
        assertEquals("did:key:z6Mkexample", response.getDid());
        assertEquals("registered", response.getStatus());
        verify(userIdentityRepository, times(1)).save(any(UserIdentity.class));
    }

    @Test
    void createDID_InvalidFormat() {
        // Arrange
        DIDCreateRequest request = new DIDCreateRequest();
        request.setDid("invalid-did");

        when(didUtil.isValidDID(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(IdentityException.class, () -> identityService.createDID(request));
    }

    @Test
    void createDID_AlreadyExists() {
        // Arrange
        DIDCreateRequest request = new DIDCreateRequest();
        request.setDid("did:key:z6Mkexample");

        when(didUtil.isValidDID(anyString())).thenReturn(true);
        when(userIdentityRepository.existsByDid(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IdentityException.class, () -> identityService.createDID(request));
    }
}