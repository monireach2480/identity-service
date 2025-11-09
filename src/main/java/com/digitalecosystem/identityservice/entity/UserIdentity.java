package com.digitalecosystem.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_identity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String did;

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_verified")
    private LocalDateTime lastVerified;

    @Column(name = "sync_status")
    private String syncStatus;

    @Column(name = "last_synced")
    private LocalDateTime lastSynced;

    @Column(name = "local_version")
    private Integer localVersion;

    @Column(name = "server_version")
    private Integer serverVersion;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (syncStatus == null) {
            syncStatus = "local_only";
        }
        if (localVersion == null) {
            localVersion = 1;
        }
        if (serverVersion == null) {
            serverVersion = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}