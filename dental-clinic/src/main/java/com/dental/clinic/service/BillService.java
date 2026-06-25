package com.dental.clinic.service;

import com.dental.clinic.dto.request.BillRequest;
import com.dental.clinic.dto.response.BillResponse;
import com.dental.clinic.exception.*;
import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillService {

    private final BillRepository billRepository;
    private final PatientRepository patientRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final NotificationService notificationService;

    public List<BillResponse> getAllBills() {
        return billRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public BillResponse getById(Long id) {
        return mapToResponse(findById(id));
    }

    public List<BillResponse> getByPatient(Long patientId) {
        return billRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<BillResponse> getByStatus(Bill.BillStatus status) {
        return billRepository.findByStatus(status).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public BillResponse createBill(BillRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));

        Bill.BillBuilder builder = Bill.builder()
                .patient(patient)
                .clinic(patient.getClinic())
                .amount(request.getAmount())
                .discount(request.getDiscount() != null ? request.getDiscount() : 0.0)
                .tax(request.getTax() != null ? request.getTax() : 0.0)
                .dueDate(request.getDueDate())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .invoiceNumber(generateInvoiceNumber())
                .status(Bill.BillStatus.UNPAID);

        if (request.getTreatmentId() != null) {
            treatmentPlanRepository.findById(request.getTreatmentId())
                    .ifPresent(builder::treatment);
        }

        Bill bill = billRepository.save(builder.build());

        if (patient.getUser() != null) {
            notificationService.createNotification(
                    patient.getUser().getId(),
                    "Invoice " + bill.getInvoiceNumber() + " generated for ₹" + bill.getTotalAmount(),
                    "New Bill", Notification.NotificationType.BILL, bill.getId());
        }

        return mapToResponse(bill);
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public BillResponse markAsPaid(Long id, String paymentMethod) {
        Bill bill = findById(id);
        bill.setStatus(Bill.BillStatus.PAID);
        bill.setPaidAt(LocalDateTime.now());
        if (paymentMethod != null) bill.setPaymentMethod(paymentMethod);
        return mapToResponse(billRepository.save(bill));
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public BillResponse updateStatus(Long id, Bill.BillStatus status) {
        Bill bill = findById(id);
        bill.setStatus(status);
        if (status == Bill.BillStatus.PAID && bill.getPaidAt() == null) {
            bill.setPaidAt(LocalDateTime.now());
        }
        return mapToResponse(billRepository.save(bill));
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public void deleteBill(Long id) {
        if (!billRepository.existsById(id)) throw new ResourceNotFoundException("Bill", id);
        billRepository.deleteById(id);
    }

    private String generateInvoiceNumber() {
        String prefix = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String unique = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + unique;
    }

    private void validateTenant(Clinic clinic) {
        Long tenantId = com.dental.clinic.security.TenantContext.getCurrentTenant();
        if (tenantId != null && clinic != null && !tenantId.equals(clinic.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Tenant isolation violation: Access denied to this clinic's data");
        }
    }

    public Bill getRawBillById(Long id) {
        return findById(id);
    }

    private Bill findById(Long id) {
        Bill bill = billRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bill", id));
        validateTenant(bill.getClinic());
        return bill;
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public BillResponse recordPayment(Long billId, com.dental.clinic.dto.request.PaymentRequest request) {
        Bill bill = findById(billId);

        Payment payment = Payment.builder()
                .bill(bill)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(request.getTransactionReference())
                .refund(request.isRefund())
                .notes(request.getNotes())
                .build();

        bill.getPayments().add(payment);


        double totalPaid = bill.getPaidAmount();
        if (totalPaid >= bill.getTotalAmount()) {
            bill.setStatus(Bill.BillStatus.PAID);
            bill.setPaidAt(LocalDateTime.now());
        } else if (totalPaid > 0) {
            bill.setStatus(Bill.BillStatus.PARTIALLY_PAID);
        } else if (totalPaid <= 0 && request.isRefund()) {
            bill.setStatus(Bill.BillStatus.REFUNDED);
        } else {
            bill.setStatus(Bill.BillStatus.UNPAID);
        }

        bill.setPaymentMethod(request.getPaymentMethod());
        billRepository.save(bill);

        return mapToResponse(bill);
    }

    private com.dental.clinic.dto.response.PaymentResponse mapPaymentToResponse(Payment p) {
        return com.dental.clinic.dto.response.PaymentResponse.builder()
                .id(p.getId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .transactionReference(p.getTransactionReference())
                .refund(p.isRefund())
                .notes(p.getNotes())
                .paidAt(p.getPaidAt())
                .build();
    }

    public BillResponse mapToResponse(Bill b) {
        List<com.dental.clinic.dto.response.PaymentResponse> paymentResponses = java.util.Collections.emptyList();
        if (b.getPayments() != null) {
            paymentResponses = b.getPayments().stream()
                    .map(this::mapPaymentToResponse)
                    .collect(Collectors.toList());
        }

        return BillResponse.builder()
                .id(b.getId())
                .patientId(b.getPatient().getId())
                .patientName(b.getPatient().getName())
                .treatmentId(b.getTreatment() != null ? b.getTreatment().getId() : null)
                .amount(b.getAmount())
                .discount(b.getDiscount())
                .tax(b.getTax())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus())
                .dueDate(b.getDueDate())
                .paidAt(b.getPaidAt())
                .paymentMethod(b.getPaymentMethod())
                .notes(b.getNotes())
                .invoiceNumber(b.getInvoiceNumber())
                .createdAt(b.getCreatedAt())
                .payments(paymentResponses)
                .paidAmount(b.getPaidAmount())
                .build();
    }
}

/*
 * This file contains the BillService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
