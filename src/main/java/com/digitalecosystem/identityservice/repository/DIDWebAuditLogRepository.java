package com.digitalecosystem.identityservice.repository;

import com.digitalecosystem.identityservice.entity.DIDWebAuditLog;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DIDWebAuditLogRepository extends JpaRepository<DIDWebAuditLog, Long> {
    List<DIDWebAuditLog> findByUserIdentityOrderByCreatedAtDesc(UserIdentity userIdentity);
    List<DIDWebAuditLog> findByPublicDid(String publicDid);
}