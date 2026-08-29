package com.dental.clinic.controller;

import com.dental.clinic.BaseIntegrationTest;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.UserRepository;
import com.dental.clinic.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
public class SecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private com.dental.clinic.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private com.dental.clinic.repository.PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@oralcare.com", roles = "ADMIN")
    void testGetActiveSessions_Authenticated() throws Exception {
        userRepository.save(User.builder()
                .email("admin@oralcare.com")
                .password("encodedPassword")
                .name("Admin User")
                .role(User.Role.ADMIN)
                .active(true)
                .build());

        mockMvc.perform(get("/sessions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetActiveSessions_Unauthenticated_ShouldFail() throws Exception {
        mockMvc.perform(get("/sessions/active"))
                .andExpect(status().is3xxRedirection());
    }
}

/*
 * This file contains the SecurityIntegrationTest class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
