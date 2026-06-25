package com.dental.clinic.service;

import com.dental.clinic.exception.TokenException;
import com.dental.clinic.model.RefreshToken;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.RefreshTokenRepository;
import com.dental.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        return createRefreshToken(userId, "unknown", "unknown");
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId, String ipAddress, String deviceDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));


        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserAndRevokedFalse(user);
        if (activeTokens.size() >= 5) {
            RefreshToken oldest = activeTokens.get(0);
            oldest.setRevoked(true);
            refreshTokenRepository.save(oldest);
        }

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .ipAddress(ipAddress)
                .deviceDetails(deviceDetails)
                .lastActive(Instant.now())
                .build();

        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            refreshTokenRepository.deleteByUser(token.getUser());
            log.warn("Revoked refresh token used by user {}. All tokens revoked.", token.getUser().getEmail());
            throw new TokenException("Refresh token was revoked. Possible replay attack. Please login again.");
        }
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenException("Refresh token has expired. Please login again.");
        }


        token.setLastActive(Instant.now());
        refreshTokenRepository.save(token);

        return token;
    }

    @Transactional
    public List<RefreshToken> getActiveSessions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return refreshTokenRepository.findByUserAndRevokedFalse(user);
    }

    @Transactional
    public void revokeToken(String tokenStr) {
        refreshTokenRepository.findByToken(tokenStr).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public void revokeTokenById(Long id, Long userId) {
        refreshTokenRepository.findById(id).ifPresent(t -> {
            if (t.getUser().getId().equals(userId)) {
                t.setRevoked(true);
                refreshTokenRepository.save(t);
            } else {
                throw new com.dental.clinic.exception.BadRequestException("Token does not belong to user");
            }
        });
    }

    @Transactional
    public void revokeByUserId(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);
            for (RefreshToken t : tokens) {
                t.setRevoked(true);
                refreshTokenRepository.save(t);
            }
        });
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        userRepository.findById(userId)
                .ifPresent(refreshTokenRepository::deleteByUser);
    }
}

/*
 * This file contains the RefreshTokenService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
