package com.digitalecosystem.identityservice.repository;

import com.digitalecosystem.identityservice.entity.BackupMetadata;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BackupMetadataRepository extends JpaRepository<BackupMetadata, Long> {
    List<BackupMetadata> findByUserIdentityOrderByCreatedAtDesc(UserIdentity userIdentity);
    Optional<BackupMetadata> findByUserIdentityAndIsLatest(UserIdentity userIdentity, Boolean isLatest);
}