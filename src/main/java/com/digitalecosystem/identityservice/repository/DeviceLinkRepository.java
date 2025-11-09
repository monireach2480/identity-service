package com.digitalecosystem.identityservice.repository;

import com.digitalecosystem.identityservice.entity.DeviceLink;
import com.digitalecosystem.identityservice.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceLinkRepository extends JpaRepository<DeviceLink, Long> {
    List<DeviceLink> findByUserIdentity(UserIdentity userIdentity);
    Optional<DeviceLink> findByDeviceId(UUID deviceId);
    List<DeviceLink> findByUserIdentityAndStatus(UserIdentity userIdentity, String status);
}