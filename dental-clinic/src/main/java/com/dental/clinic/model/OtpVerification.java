package com.dental.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiry;

    @Column(name = "is_used")
    @Builder.Default
    private boolean used = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false)
    @Builder.Default
    private OtpType otpType = OtpType.REGISTRATION;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiry);
    }

    public enum OtpType {
        REGISTRATION, PASSWORD_RESET
    }
}