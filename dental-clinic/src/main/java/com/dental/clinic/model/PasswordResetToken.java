package com.dental.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "otp")
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiry;

    @Column(name = "is_used")
    @Builder.Default
    private boolean used = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type")
    @Builder.Default
    private TokenType tokenType = TokenType.EMAIL_LINK;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "attempts")
    @Builder.Default
    private Integer attempts = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiry);
    }

    public enum TokenType {
        EMAIL_LINK, OTP_EMAIL, OTP_SMS
    }
}

/*
 * This file contains the PasswordResetToken class which manages JPA persistence entities mapping database tables to Java object structures.
 * Concepts used: JPA/ORM annotations, database schema definitions, and Lombok builders.
 */
