package com.dental.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_history")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @CreatedDate
    @Column(name = "login_time", updatable = false, nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "device_details")
    private String deviceDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginStatus status;

    public enum LoginStatus {
        SUCCESS, FAILED_BAD_CREDENTIALS, FAILED_LOCKED
    }
}

/*
 * This file contains the LoginHistory class which manages JPA persistence entities mapping database tables to Java object structures.
 * Concepts used: JPA/ORM annotations, database schema definitions, and Lombok builders.
 */
