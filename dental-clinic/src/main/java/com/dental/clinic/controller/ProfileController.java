package com.dental.clinic.controller;

import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.exception.BadRequestException;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.Patient;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.PatientRepository;
import com.dental.clinic.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;











@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile", description = "Role-based profile management — fields driven by entity model")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final UserRepository    userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder   passwordEncoder;



    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's profile",
            description = "Returns User fields for all roles. PATIENT role also returns Patient entity fields.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(Authentication auth) {
        User user = resolveUser(auth);
        Map<String, Object> profile = buildUserProfile(user);

        if (user.getRole() == User.Role.PATIENT) {
            patientRepository.findByUserId(user.getId()).ifPresent(p -> {
                profile.put("patientId",       p.getId());
                profile.put("dateOfBirth", p.getDateOfBirth());
                profile.put("contact",         p.getContact());
                profile.put("gender",          p.getGender());
                profile.put("bloodGroup",      p.getBloodGroup());
                profile.put("medicalHistory",  p.getMedicalHistory());
                profile.put("allergies",       p.getAllergies());
                profile.put("emergencyContact",p.getEmergencyContact());
                profile.put("address",         p.getAddress());
            });
        }

        return ResponseEntity.ok(ApiResponse.success(profile));
    }



    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update profile",
            description = "Updates ONLY fields present in the entity. " +
                    "PATIENT: also updates Patient record (age, contact, gender, bloodGroup, medicalHistory, allergies, emergencyContact, address). " +
                    "DENTIST/RECEPTIONIST/ADMIN: updates User record (name, phoneNumber).")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            Authentication auth,
            @RequestBody Map<String, Object> updates) {

        User user = resolveUser(auth);


        if (updates.containsKey("name")) {
            String name = (String) updates.get("name");
            if (name != null && !name.isBlank()) user.setName(name);
        }
        if (updates.containsKey("phoneNumber")) {
            user.setPhoneNumber((String) updates.get("phoneNumber"));
        }

        userRepository.save(user);


        if (user.getRole() == User.Role.PATIENT) {
            Patient patient = patientRepository.findByUserId(user.getId()).orElse(null);
            if (patient != null) {
                if (updates.containsKey("dateOfBirth")) {
                    patient.setDateOfBirth(LocalDate.parse((String) updates.get("dateOfBirth")));
                }
                if (updates.containsKey("contact"))         patient.setContact((String) updates.get("contact"));
                if (updates.containsKey("gender"))          patient.setGender((String) updates.get("gender"));
                if (updates.containsKey("bloodGroup"))      patient.setBloodGroup((String) updates.get("bloodGroup"));
                if (updates.containsKey("medicalHistory"))  patient.setMedicalHistory((String) updates.get("medicalHistory"));
                if (updates.containsKey("allergies"))       patient.setAllergies((String) updates.get("allergies"));
                if (updates.containsKey("emergencyContact"))patient.setEmergencyContact((String) updates.get("emergencyContact"));
                if (updates.containsKey("address"))         patient.setAddress((String) updates.get("address"));
                patientRepository.save(patient);
            }
        }

        log.info("Profile updated: userId={}, role={}", user.getId(), user.getRole());
        return getProfile(auth);
    }



    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change own password",
            description = "Requires currentPassword and newPassword (min 8 chars).")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication auth,
            @RequestBody Map<String, String> body) {

        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");

        if (currentPassword == null || currentPassword.isBlank())
            throw new BadRequestException("currentPassword is required.");
        if (newPassword == null || newPassword.length() < 8)
            throw new BadRequestException("newPassword must be at least 8 characters.");

        User user = resolveUser(auth);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for userId={}", user.getId());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully.", null));
    }



    private User resolveUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    private Map<String, Object> buildUserProfile(User user) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id",            user.getId());
        p.put("name",          user.getName());
        p.put("email",         user.getEmail());
        p.put("role",          user.getRole());
        p.put("phoneNumber",   user.getPhoneNumber());
        p.put("active",        user.isActive());
        p.put("emailVerified", user.isEmailVerified());
        p.put("clinicId",      user.getClinicId());
        p.put("createdAt",     user.getCreatedAt());
        return p;
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        try { return Integer.parseInt(val.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}

/*
 * This file contains the ProfileController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
