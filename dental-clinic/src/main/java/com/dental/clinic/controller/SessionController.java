package com.dental.clinic.controller;

import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Active session tracking and device management")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {

    private final RefreshTokenService refreshTokenService;
    private final com.dental.clinic.repository.UserRepository userRepository;

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active sessions/devices for the logged-in user")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getActiveSessions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        com.dental.clinic.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SessionResponse> sessions = refreshTokenService.getActiveSessions(user.getId()).stream()
                .map(t -> SessionResponse.builder()
                        .id(t.getId())
                        .ipAddress(t.getIpAddress())
                        .deviceDetails(t.getDeviceDetails())
                        .lastActive(t.getLastActive())
                        .expiryDate(t.getExpiryDate())
                        .tokenPreview(t.getToken().substring(0, Math.min(t.getToken().length(), 8)) + "...")
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @PostMapping("/revoke/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Revoke/logout a specific session by ID")
    public ResponseEntity<ApiResponse<Void>> revokeSession(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        com.dental.clinic.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenService.revokeTokenById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Session revoked successfully", null));
    }

    @PostMapping("/revoke-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Revoke/logout all active sessions")
    public ResponseEntity<ApiResponse<Void>> revokeAllSessions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        com.dental.clinic.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenService.revokeByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success("All sessions revoked successfully", null));
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SessionResponse {
        private Long id;
        private String ipAddress;
        private String deviceDetails;
        private java.time.Instant lastActive;
        private java.time.Instant expiryDate;
        private String tokenPreview;
    }
}

/*
 * This file contains the SessionController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
