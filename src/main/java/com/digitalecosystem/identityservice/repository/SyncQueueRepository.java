package com.digitalecosystem.identityservice.repository;

import com.digitalecosystem.identityservice.entity.SyncQueue;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SyncQueueRepository extends JpaRepository<SyncQueue, Long> {
    List<SyncQueue> findByUserIdentityAndSyncedFalse(UserIdentity userIdentity);
    List<SyncQueue> findBySyncedFalseOrderByClientTimestampAsc();
}