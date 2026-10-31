package com.dental.clinic.controller;

import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.UserResponse;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.UserRepository;
import com.dental.clinic.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management — view, activate/deactivate staff accounts")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(authService::mapToUserResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return ResponseEntity.ok(ApiResponse.success(authService.mapToUserResponse(user)));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Get users by role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getByRole(@PathVariable User.Role role) {
        List<UserResponse> users = userRepository.findByRole(role).stream()
                .map(authService::mapToUserResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/dentists")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    @Operation(summary = "Get all dentists")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getDentists() {
        List<UserResponse> dentists = userRepository.findByRole(User.Role.DENTIST).stream()
                .filter(User::isActive)
                .map(authService::mapToUserResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dentists));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User deactivated", null));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate user account")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User activated", null));
    }
}

/*
 * This file contains the UserController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
