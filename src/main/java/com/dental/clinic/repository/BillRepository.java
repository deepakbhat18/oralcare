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

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Bill b WHERE " +
           "b.status = 'PAID' AND " +
           "(cast(:dentistId as Long) IS NULL OR b.appointment.dentist.id = :dentistId) AND " +
           "(cast(:clinicId as Long) IS NULL OR b.clinic.id = :clinicId) AND " +
           "(cast(:start as LocalDateTime) IS NULL OR b.paidAt >= :start) AND " +
           "(cast(:end as LocalDateTime) IS NULL OR b.paidAt <= :end)")
    Double sumRevenueFiltered(
            @org.springframework.data.repository.query.Param("dentistId") Long dentistId,
            @org.springframework.data.repository.query.Param("clinicId") Long clinicId,
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) - COALESCE((SELECT SUM(p.amount) FROM Payment p WHERE p.bill.status IN ('UNPAID', 'PARTIALLY_PAID') AND (cast(:dentistId as Long) IS NULL OR p.bill.appointment.dentist.id = :dentistId) AND (cast(:clinicId as Long) IS NULL OR p.bill.clinic.id = :clinicId)), 0.0) FROM Bill b WHERE " +
           "b.status IN ('UNPAID', 'PARTIALLY_PAID') AND " +
           "(cast(:dentistId as Long) IS NULL OR b.appointment.dentist.id = :dentistId) AND " +
           "(cast(:clinicId as Long) IS NULL OR b.clinic.id = :clinicId)")
    Double getOutstandingAmountFiltered(
            @org.springframework.data.repository.query.Param("dentistId") Long dentistId,
            @org.springframework.data.repository.query.Param("clinicId") Long clinicId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(b) FROM Bill b WHERE " +
           "b.status = :status AND " +
           "(cast(:dentistId as Long) IS NULL OR b.appointment.dentist.id = :dentistId) AND " +
           "(cast(:clinicId as Long) IS NULL OR b.clinic.id = :clinicId)")
    long countInvoicesByStatusFiltered(
            @org.springframework.data.repository.query.Param("status") Bill.BillStatus status,
            @org.springframework.data.repository.query.Param("dentistId") Long dentistId,
            @org.springframework.data.repository.query.Param("clinicId") Long clinicId);

    @org.springframework.data.jpa.repository.Query("SELECT b.clinic.name, COALESCE(SUM(b.totalAmount), 0.0) FROM Bill b WHERE b.status = 'PAID' GROUP BY b.clinic.name")
    List<Object[]> getRevenueByClinic();

    @org.springframework.data.jpa.repository.Query("SELECT b.appointment.dentist.name, COALESCE(SUM(b.totalAmount), 0.0) FROM Bill b WHERE b.status = 'PAID' AND b.appointment IS NOT NULL GROUP BY b.appointment.dentist.name")
    List<Object[]> getRevenueByDentist();
}

/*
 * This file contains the BillRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
