package com.digitalecosystem.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "backup_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserIdentity userIdentity;

    @Column(name = "backup_version", length = 20)
    private String backupVersion;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "storage_path", columnDefinition = "TEXT")
    private String storagePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_latest")
    private Boolean isLatest;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isLatest == null) {
            isLatest = true;
        }
    }
}