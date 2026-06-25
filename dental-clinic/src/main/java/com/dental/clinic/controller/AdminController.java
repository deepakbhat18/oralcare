package com.dental.clinic.controller;

import com.dental.clinic.dto.request.RegisterRequest;
import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.UserResponse;
import com.dental.clinic.exception.BadRequestException;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.AppointmentRepository;
import com.dental.clinic.repository.PatientRepository;
import com.dental.clinic.repository.UserRepository;
import com.dental.clinic.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Full system management — ADMIN role only")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserRepository        userRepository;
    private final PatientRepository     patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuthService           authService;
    private final PasswordEncoder       passwordEncoder;



    @GetMapping("/profile")
    @Operation(summary = "Get admin's own profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success(authService.mapToUserResponse(getCurrentAdmin())));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update admin profile (name, email, phone, password)")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@RequestBody Map<String, String> updates) {
        User admin = getCurrentAdmin();

        if (updates.containsKey("name") && !updates.get("name").isBlank())
            admin.setName(updates.get("name"));

        if (updates.containsKey("email") && !updates.get("email").isBlank()) {
            String newEmail = updates.get("email");
            if (!newEmail.equals(admin.getEmail()) && userRepository.existsByEmail(newEmail))
                throw new BadRequestException("Email already in use: " + newEmail);
            admin.setEmail(newEmail);
        }

        if (updates.containsKey("phoneNumber"))
            admin.setPhoneNumber(updates.get("phoneNumber"));

        if (updates.containsKey("newPassword") && !updates.get("newPassword").isBlank()) {
            String currentPassword = updates.get("currentPassword");
            if (currentPassword == null || currentPassword.isBlank())
                throw new BadRequestException("Current password is required to change password");
            if (!passwordEncoder.matches(currentPassword, admin.getPassword()))
                throw new BadRequestException("Current password is incorrect");
            if (updates.get("newPassword").length() < 8)
                throw new BadRequestException("New password must be at least 8 characters");
            admin.setPassword(passwordEncoder.encode(updates.get("newPassword")));
        }

        userRepository.save(admin);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", authService.mapToUserResponse(admin)));
    }



    @GetMapping("/dashboard")
    @Operation(summary = "Admin dashboard KPIs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers",            userRepository.count());
        data.put("totalPatients",         patientRepository.count());
        data.put("totalDentists",         (long) userRepository.findByRole(User.Role.DENTIST).size());
        data.put("totalReceptionists",    (long) userRepository.findByRole(User.Role.RECEPTIONIST).size());
        data.put("totalAppointments",     appointmentRepository.count());
        data.put("todayAppointments",     appointmentRepository.countToday(LocalDateTime.now()));
        data.put("completedAppointments", appointmentRepository.countCompleted());
        data.put("activeUsers",           userRepository.findAll().stream().filter(User::isActive).count());
        data.put("generatedAt",           LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success(data));
    }



    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(
                userRepository.findAll().stream().map(authService::mapToUserResponse).collect(Collectors.toList())));
    }

    @GetMapping("/users/role/{role}")
    @Operation(summary = "Get users by role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable User.Role role) {
        return ResponseEntity.ok(ApiResponse.success(
                userRepository.findByRole(role).stream().map(authService::mapToUserResponse).collect(Collectors.toList())));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        return ResponseEntity.ok(ApiResponse.success(authService.mapToUserResponse(user)));
    }

    @PostMapping("/users")
    @Operation(summary = "Create a new user (any role)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created", authService.register(request)));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id, @RequestBody Map<String, Object> updates) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (updates.containsKey("name"))        user.setName((String) updates.get("name"));
        if (updates.containsKey("phoneNumber")) user.setPhoneNumber((String) updates.get("phoneNumber"));
        if (updates.containsKey("clinicId"))    user.setClinicId(Long.valueOf(updates.get("clinicId").toString()));
        if (updates.containsKey("role")) {
            try { user.setRole(User.Role.valueOf((String) updates.get("role"))); }
            catch (IllegalArgumentException e) { throw new BadRequestException("Invalid role"); }
        }
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User updated", authService.mapToUserResponse(user)));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(true); userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User activated", null));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(false); userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User deactivated", null));
    }

    @PatchMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 8)
            throw new BadRequestException("New password must be at least 8 characters.");
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Password reset", null));
    }










@DeleteMapping("/users/{id}")
@Operation(summary = "Deactivate a user (soft delete)")
public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {

    User admin = getCurrentAdmin();

    if (admin.getId().equals(id)) {
        throw new BadRequestException("You cannot delete your own account.");
    }

    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));


    user.setActive(false);
    userRepository.save(user);

    return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
}

    @GetMapping("/stats/users-by-role")
    public ResponseEntity<ApiResponse<Map<String, Long>>> usersByRole() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (User.Role role : User.Role.values())
            counts.put(role.name(), (long) userRepository.findByRole(role).size());
        return ResponseEntity.ok(ApiResponse.success(counts));
    }

    private User getCurrentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
    }
}

/*
 * This file contains the AdminController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
