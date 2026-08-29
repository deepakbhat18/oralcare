package com.dental.clinic.controller;

import com.dental.clinic.BaseIntegrationTest;
import com.dental.clinic.dto.request.PaymentRequest;
import com.dental.clinic.model.Bill;
import com.dental.clinic.model.Clinic;
import com.dental.clinic.model.Patient;
import com.dental.clinic.repository.BillRepository;
import com.dental.clinic.repository.ClinicRepository;
import com.dental.clinic.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
public class BillingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Bill testBill;

    @BeforeEach
    void setUp() {
        billRepository.deleteAll();
        patientRepository.deleteAll();
        clinicRepository.deleteAll();

        Clinic clinic = clinicRepository.save(Clinic.builder()
                .name("Test Clinic")
                .address("123 Test St")
                .contact("1234567890")
                .build());

        Patient patient = patientRepository.save(Patient.builder()
                .name("John Doe")
                .contact("9876543210")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .clinic(clinic)
                .build());

        testBill = billRepository.save(Bill.builder()
                .patient(patient)
                .clinic(clinic)
                .amount(1000.0)
                .discount(100.0)
                .tax(50.0)
                .status(Bill.BillStatus.UNPAID)
                .invoiceNumber("INV-999999")
                .dueDate(LocalDateTime.now().plusDays(10))
                .build());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @org.springframework.transaction.annotation.Transactional
    void testRecordPayment_Success() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .amount(500.0)
                .paymentMethod("CREDIT_CARD")
                .transactionReference("TXN-12345")
                .notes("Partial payment")
                .refund(false)
                .build();

        mockMvc.perform(post("/bills/" + testBill.getId() + "/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.data.paidAmount").value(500.0));

        Bill updatedBill = billRepository.findById(testBill.getId()).orElseThrow();
        assertThat(updatedBill.getStatus()).isEqualTo(Bill.BillStatus.PARTIALLY_PAID);
        assertThat(updatedBill.getPayments()).hasSize(1);
    }
}

/*
 * This file contains the BillingIntegrationTest class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
