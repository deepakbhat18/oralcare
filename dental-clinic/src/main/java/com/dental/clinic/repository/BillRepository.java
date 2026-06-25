package com.dental.clinic.repository;
import com.dental.clinic.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPatientId(Long patientId);
    List<Bill> findByStatus(Bill.BillStatus status);
    long countByStatus(Bill.BillStatus status);
    Optional<Bill> findByInvoiceNumber(String invoiceNumber);
    List<Bill> findByPatientIdAndStatus(Long patientId, Bill.BillStatus status);
    List<Bill> findByClinicId(Long clinicId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Bill b WHERE b.status = 'PAID'")
    Double sumTotalPaidRevenue();

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Bill b WHERE b.status = 'PAID' AND b.paidAt >= :start AND b.paidAt <= :end")
    Double sumRevenueInPeriod(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}

/*
 * This file contains the BillRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
