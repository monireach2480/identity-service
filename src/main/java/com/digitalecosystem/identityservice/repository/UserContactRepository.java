package com.digitalecosystem.identityservice.repository;

import com.digitalecosystem.identityservice.entity.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserContactRepository extends JpaRepository<UserContact, Long> {
    Optional<UserContact> findByEmail(String email);
    Optional<UserContact> findByPhoneNumber(String phoneNumber);
    Optional<UserContact> findByEmailOrPhoneNumber(String email, String phoneNumber);
    Optional<UserContact> findByIdentifierHash(String identifierHash);

}