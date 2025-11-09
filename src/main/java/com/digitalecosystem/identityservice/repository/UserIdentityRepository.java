package com.digitalecosystem.identityservice.repository;

import com.digitalecosystem.identityservice.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
    Optional<UserIdentity> findByDid(String did);
    boolean existsByDid(String did);
}