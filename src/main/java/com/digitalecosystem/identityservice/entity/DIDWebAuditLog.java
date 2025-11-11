package com.digitalecosystem.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "did_web_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DIDWebAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserIdentity userIdentity;

    @Column(nullable = false, length = 50)
    private String operation;

    @Column(name = "public_did")
    private String publicDid;

    @Column(length = 20)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details")
    private Map<String, Object> details;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}