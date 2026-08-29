package com.dental.clinic.controller;

import com.dental.clinic.BaseIntegrationTest;
import com.dental.clinic.model.Appointment;
import com.dental.clinic.model.Bill;
import com.dental.clinic.model.Clinic;
import com.dental.clinic.model.Patient;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.AppointmentRepository;
import com.dental.clinic.repository.BillRepository;
import com.dental.clinic.repository.ClinicRepository;
import com.dental.clinic.repository.PatientRepository;
import com.dental.clinic.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class SmartAnalyticsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        billRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
        clinicRepository.deleteAll();

        Clinic clinic = clinicRepository.save(Clinic.builder()
                .name("Analytics Test Clinic")
                .address("456 Analytics Rd")
                .contact("0987654321")
                .build());

        User dentist = userRepository.save(User.builder()
                .name("Dr. Smith")
                .email("smith@dental.com")
                .password("password")
                .role(User.Role.DENTIST)
                .clinicId(clinic.getId())
                .build());

        Patient patient = patientRepository.save(Patient.builder()
                .name("Alice Cooper")
                .contact("555-0199")
                .dateOfBirth(LocalDate.of(1985, 5, 20))
                .gender("FEMALE")
                .clinic(clinic)
                .build());

        LocalDateTime now = LocalDateTime.now();

        // Create completed appointment with wait and treatment times
        Appointment appt1 = appointmentRepository.save(Appointment.builder()
                .patient(patient)
                .dentist(dentist)
                .clinic(clinic)
                .dateTime(now.minusHours(2))
                .durationMinutes(30)
                .treatmentType("CLEANING")
                .status(Appointment.AppointmentStatus.COMPLETED)
                .checkInTime(now.minusHours(2))
                .treatmentStartTime(now.minusHours(2).plusMinutes(15)) // 15 mins wait
                .treatmentEndTime(now.minusHours(2).plusMinutes(45)) // 30 mins duration
                .build());

        // Create associated paid bill to enable revenue analytics
        billRepository.save(Bill.builder()
                .appointment(appt1)
                .patient(patient)
                .clinic(clinic)
                .amount(1500.0)
                .discount(100.0)
                .tax(50.0)
                .status(Bill.BillStatus.PAID)
                .invoiceNumber("INV-000001")
                .paidAt(now.minusHours(1))
                .dueDate(now.plusDays(5))
                .build());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetDashboardSummary_Success() throws Exception {
        mockMvc.perform(get("/analytics/dashboard-summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.operationalSummary").exists())
                .andExpect(jsonPath("$.data.businessSummary").exists())
                .andExpect(jsonPath("$.data.performanceSummary").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetWaitTimeAnalytics_Success() throws Exception {
        mockMvc.perform(get("/analytics/wait-time")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.averageWaitingTime").value(15.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetTreatmentAnalytics_Success() throws Exception {
        mockMvc.perform(get("/analytics/treatments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mostCommonTreatment").exists())
                .andExpect(jsonPath("$.data.averageTreatmentDurationMinutes").value(30.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBusinessInsights_Success() throws Exception {
        mockMvc.perform(get("/analytics/business-insights")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
