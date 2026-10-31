package com.dental.clinic.repository;

import com.dental.clinic.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface AnalyticsRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.dateTime >= :start")
    long countAppointmentsSince(@Param("start") LocalDateTime start);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'COMPLETED'")
    long countCompleted();

    @Query("""
        SELECT FUNCTION('TO_CHAR', a.dateTime, 'YYYY-MM') as ym, COUNT(a)
        FROM Appointment a
        WHERE a.dateTime >= :start
        GROUP BY FUNCTION('TO_CHAR', a.dateTime, 'YYYY-MM')
        ORDER BY ym
        """)
    List<Object[]> countByMonth(@Param("start") LocalDateTime start);

    @Query("""
        SELECT a.treatmentType, COUNT(a)
        FROM Appointment a
        WHERE a.treatmentType IS NOT NULL
        GROUP BY a.treatmentType
        ORDER BY COUNT(a) DESC
        """)
    List<Object[]> countByTreatmentType();

    @Query("""
        SELECT CAST(a.dateTime AS LocalDate), COUNT(a), SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END)
        FROM Appointment a
        WHERE a.dateTime >= :start AND a.dateTime < :end
        GROUP BY CAST(a.dateTime AS LocalDate)
        ORDER BY CAST(a.dateTime AS LocalDate)
        """)
    List<Object[]> dailyCountsInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT AVG(a.estimatedWaitMinutes)
        FROM Appointment a
        WHERE a.estimatedWaitMinutes IS NOT NULL
          AND a.dateTime >= :start AND a.dateTime < :end
        """)
    Double avgQueueTimeInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT COUNT(a)
        FROM Appointment a
        WHERE a.dentist.id = :dentistId
          AND a.dateTime >= :start
        """)
    long countByDentistSince(@Param("dentistId") Long dentistId, @Param("start") LocalDateTime start);

    @Query("""
        SELECT COUNT(a)
        FROM Appointment a
        WHERE a.dentist.id = :dentistId
          AND a.status = 'COMPLETED'
        """)
    long countCompletedByDentist(@Param("dentistId") Long dentistId);

    @Query("""
        SELECT COUNT(a)
        FROM Appointment a
        WHERE a.dentist.id = :dentistId
          AND a.dateTime >= :dayStart AND a.dateTime < :dayEnd
        """)
    long countByDentistToday(@Param("dentistId") Long dentistId,
                             @Param("dayStart") LocalDateTime dayStart,
                             @Param("dayEnd") LocalDateTime dayEnd);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.dentist.id = :dentistId")
    long countTotalByDentist(@Param("dentistId") Long dentistId);

    // Wait Time queries
    @Query(value = "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (a.treatment_start_time - a.check_in_time)) / 60.0), 0.0) FROM appointments a WHERE a.check_in_time IS NOT NULL AND a.treatment_start_time IS NOT NULL AND a.deleted = false AND (cast(:dentistId as bigint) IS NULL OR a.dentist_id = :dentistId) AND (cast(:clinicId as bigint) IS NULL OR a.clinic_id = :clinicId) AND (cast(:start as timestamp) IS NULL OR a.treatment_start_time >= :start) AND (cast(:end as timestamp) IS NULL OR a.treatment_start_time <= :end)", nativeQuery = true)
    Double getActualAvgWaitTimeFiltered(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT COALESCE(MAX(EXTRACT(EPOCH FROM (a.treatment_start_time - a.check_in_time)) / 60.0), 0.0) FROM appointments a WHERE a.check_in_time IS NOT NULL AND a.treatment_start_time IS NOT NULL AND a.deleted = false AND (cast(:dentistId as bigint) IS NULL OR a.dentist_id = :dentistId) AND (cast(:clinicId as bigint) IS NULL OR a.clinic_id = :clinicId) AND (cast(:start as timestamp) IS NULL OR a.treatment_start_time >= :start) AND (cast(:end as timestamp) IS NULL OR a.treatment_start_time <= :end)", nativeQuery = true)
    Double getActualMaxWaitTimeFiltered(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT COALESCE(MIN(EXTRACT(EPOCH FROM (a.treatment_start_time - a.check_in_time)) / 60.0), 0.0) FROM appointments a WHERE a.check_in_time IS NOT NULL AND a.treatment_start_time IS NOT NULL AND a.deleted = false AND (cast(:dentistId as bigint) IS NULL OR a.dentist_id = :dentistId) AND (cast(:clinicId as bigint) IS NULL OR a.clinic_id = :clinicId) AND (cast(:start as timestamp) IS NULL OR a.treatment_start_time >= :start) AND (cast(:end as timestamp) IS NULL OR a.treatment_start_time <= :end)", nativeQuery = true)
    Double getActualMinWaitTimeFiltered(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT u.name, COALESCE(AVG(EXTRACT(EPOCH FROM (a.treatment_start_time - a.check_in_time)) / 60.0), 0.0) FROM appointments a JOIN users u ON a.dentist_id = u.id WHERE a.check_in_time IS NOT NULL AND a.treatment_start_time IS NOT NULL AND a.deleted = false GROUP BY u.name", nativeQuery = true)
    List<Object[]> getAvgWaitTimePerDentist();

    @Query(value = "SELECT c.name, COALESCE(AVG(EXTRACT(EPOCH FROM (a.treatment_start_time - a.check_in_time)) / 60.0), 0.0) FROM appointments a JOIN clinics c ON a.clinic_id = c.id WHERE a.check_in_time IS NOT NULL AND a.treatment_start_time IS NOT NULL AND a.deleted = false GROUP BY c.name", nativeQuery = true)
    List<Object[]> getAvgWaitTimePerClinic();

    // Treatment duration
    @Query(value = "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (a.treatment_end_time - a.treatment_start_time)) / 60.0), 0.0) FROM appointments a WHERE a.treatment_start_time IS NOT NULL AND a.treatment_end_time IS NOT NULL AND a.deleted = false AND (cast(:dentistId as bigint) IS NULL OR a.dentist_id = :dentistId) AND (cast(:clinicId as bigint) IS NULL OR a.clinic_id = :clinicId) AND (cast(:start as timestamp) IS NULL OR a.treatment_start_time >= :start) AND (cast(:end as timestamp) IS NULL OR a.treatment_start_time <= :end)", nativeQuery = true)
    Double getAvgTreatmentDurationFiltered(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Peak booking hours/days
    @Query(value = "SELECT EXTRACT(HOUR FROM a.date_time) as hr, COUNT(a.id) FROM appointments a WHERE a.deleted = false AND (cast(:dentistId as bigint) IS NULL OR a.dentist_id = :dentistId) AND (cast(:clinicId as bigint) IS NULL OR a.clinic_id = :clinicId) GROUP BY hr ORDER BY hr", nativeQuery = true)
    List<Object[]> getPeakBookingHours(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId);

    @Query(value = "SELECT EXTRACT(DOW FROM a.date_time) as dow, COUNT(a.id) FROM appointments a WHERE a.deleted = false AND (cast(:dentistId as bigint) IS NULL OR a.dentist_id = :dentistId) AND (cast(:clinicId as bigint) IS NULL OR a.clinic_id = :clinicId) GROUP BY dow ORDER BY dow", nativeQuery = true)
    List<Object[]> getPeakBookingDays(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId);

    // Dentist performance native query
    @Query(value = "SELECT u.id, u.name, " +
           "COUNT(DISTINCT a.patient_id) as patients_treated, " +
           "SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) as treatments_completed, " +
           "COALESCE(AVG(CASE WHEN a.treatment_start_time IS NOT NULL AND a.treatment_end_time IS NOT NULL THEN (EXTRACT(EPOCH FROM (a.treatment_end_time - a.treatment_start_time)) / 60.0) ELSE a.duration_minutes END), 0.0) as avg_duration, " +
           "COALESCE(AVG(CASE WHEN a.check_in_time IS NOT NULL AND a.treatment_start_time IS NOT NULL THEN (EXTRACT(EPOCH FROM (a.treatment_start_time - a.check_in_time)) / 60.0) ELSE a.estimated_wait_minutes END), 0.0) as avg_wait, " +
           "COALESCE(SUM(CASE WHEN b.status = 'PAID' THEN b.total_amount ELSE 0.0 END), 0.0) as revenue, " +
           "COALESCE(SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(a.id), 0), 0.0) as completion_rate, " +
           "COALESCE(SUM(CASE WHEN a.status = 'CANCELLED' THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(a.id), 0), 0.0) as cancellation_rate " +
           "FROM appointments a " +
           "JOIN users u ON a.dentist_id = u.id " +
           "LEFT JOIN bills b ON b.appointment_id = a.id " +
           "WHERE a.deleted = false AND (cast(:clinicId as bigint) IS NULL OR a.clinic_id = :clinicId) " +
           "GROUP BY u.id, u.name", nativeQuery = true)
    List<Object[]> getDentistPerformanceStats(@Param("clinicId") Long clinicId);

    // Treatment distribution native queries
    @Query(value = "SELECT a.treatment_type, COALESCE(SUM(b.total_amount), 0.0) FROM bills b JOIN appointments a ON b.appointment_id = a.id WHERE b.status = 'PAID' AND (cast(:dentistId as bigint) IS NULL OR a.dentist_id = :dentistId) AND (cast(:clinicId as bigint) IS NULL OR a.clinic_id = :clinicId) GROUP BY a.treatment_type", nativeQuery = true)
    List<Object[]> getRevenuePerTreatment(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId);

    @Query("SELECT a.treatmentType, COUNT(a) FROM Appointment a WHERE " +
           "a.treatmentType IS NOT NULL AND " +
           "(cast(:dentistId as Long) IS NULL OR a.dentist.id = :dentistId) AND " +
           "(cast(:clinicId as Long) IS NULL OR a.clinic.id = :clinicId) AND " +
           "(cast(:start as LocalDateTime) IS NULL OR a.dateTime >= :start) AND " +
           "(cast(:end as LocalDateTime) IS NULL OR a.dateTime <= :end) " +
           "GROUP BY a.treatmentType ORDER BY COUNT(a) DESC")
    List<Object[]> getTreatmentCounts(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('TO_CHAR', a.dateTime, 'YYYY-MM') as ym, a.treatmentType, COUNT(a) " +
           "FROM Appointment a WHERE " +
           "(cast(:dentistId as Long) IS NULL OR a.dentist.id = :dentistId) AND " +
           "(cast(:clinicId as Long) IS NULL OR a.clinic.id = :clinicId) AND " +
           "(cast(:start as LocalDateTime) IS NULL OR a.dateTime >= :start) AND " +
           "(cast(:end as LocalDateTime) IS NULL OR a.dateTime <= :end) " +
           "GROUP BY ym, a.treatmentType ORDER BY ym")
    List<Object[]> getTreatmentDistributionByMonth(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT a.dentist.name, a.treatmentType, COUNT(a) " +
           "FROM Appointment a WHERE " +
           "(cast(:clinicId as Long) IS NULL OR a.clinic.id = :clinicId) AND " +
           "(cast(:start as LocalDateTime) IS NULL OR a.dateTime >= :start) AND " +
           "(cast(:end as LocalDateTime) IS NULL OR a.dateTime <= :end) " +
           "GROUP BY a.dentist.name, a.treatmentType")
    List<Object[]> getTreatmentDistributionByDentist(
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Queue wait time trend
    @Query(value = "SELECT CAST(a.treatment_start_time AS DATE) as date, COALESCE(AVG(EXTRACT(EPOCH FROM (a.treatment_start_time - a.check_in_time)) / 60.0), 0.0) FROM appointments a WHERE a.check_in_time IS NOT NULL AND a.treatment_start_time IS NOT NULL AND a.deleted = false AND (cast(:dentistId as bigint) IS NULL OR a.dentist_id = :dentistId) AND (cast(:clinicId as bigint) IS NULL OR a.clinic_id = :clinicId) AND (cast(:start as timestamp) IS NULL OR a.treatment_start_time >= :start) AND (cast(:end as timestamp) IS NULL OR a.treatment_start_time <= :end) GROUP BY date ORDER BY date", nativeQuery = true)
    List<Object[]> getDailyWaitTimeTrend(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT TO_CHAR(a.treatment_start_time, 'YYYY-MM') as ym, COALESCE(AVG(EXTRACT(EPOCH FROM (a.treatment_start_time - a.check_in_time)) / 60.0), 0.0) FROM appointments a WHERE a.check_in_time IS NOT NULL AND a.treatment_start_time IS NOT NULL AND a.deleted = false AND (cast(:dentistId as bigint) IS NULL OR a.dentist_id = :dentistId) AND (cast(:clinicId as bigint) IS NULL OR a.clinic_id = :clinicId) AND (cast(:start as timestamp) IS NULL OR a.treatment_start_time >= :start) GROUP BY ym ORDER BY ym", nativeQuery = true)
    List<Object[]> getMonthlyWaitTimeTrend(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start);

    // Filtered count queries
    @Query("SELECT COUNT(a) FROM Appointment a WHERE " +
           "(cast(:dentistId as Long) IS NULL OR a.dentist.id = :dentistId) AND " +
           "(cast(:clinicId as Long) IS NULL OR a.clinic.id = :clinicId) AND " +
           "(cast(:start as LocalDateTime) IS NULL OR a.dateTime >= :start) AND " +
           "(cast(:end as LocalDateTime) IS NULL OR a.dateTime <= :end)")
    long countAppointmentsFiltered(
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE " +
           "a.status = :status AND " +
           "(cast(:dentistId as Long) IS NULL OR a.dentist.id = :dentistId) AND " +
           "(cast(:clinicId as Long) IS NULL OR a.clinic.id = :clinicId) AND " +
           "(cast(:start as LocalDateTime) IS NULL OR a.dateTime >= :start) AND " +
           "(cast(:end as LocalDateTime) IS NULL OR a.dateTime <= :end)")
    long countAppointmentsByStatusFiltered(
            @Param("status") Appointment.AppointmentStatus status,
            @Param("dentistId") Long dentistId,
            @Param("clinicId") Long clinicId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Patient Analytics queries
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.createdAt >= :start AND (cast(:clinicId as Long) IS NULL OR p.clinic.id = :clinicId)")
    long countNewPatients(@Param("start") LocalDateTime start, @Param("clinicId") Long clinicId);

    @Query("SELECT COUNT(p) FROM Patient p WHERE (cast(:clinicId as Long) IS NULL OR p.clinic.id = :clinicId) AND (SELECT COUNT(a) FROM Appointment a WHERE a.patient = p AND a.status = 'COMPLETED') > 1")
    long countReturningPatients(@Param("clinicId") Long clinicId);

    @Query("SELECT a.patient.name, COUNT(a) FROM Appointment a WHERE a.status = 'COMPLETED' AND (cast(:clinicId as Long) IS NULL OR a.clinic.id = :clinicId) GROUP BY a.patient.name ORDER BY COUNT(a) DESC")
    List<Object[]> getMostFrequentPatients(@Param("clinicId") Long clinicId);

    @Query("SELECT a.patient.name, COUNT(a) FROM Appointment a WHERE a.status = 'CANCELLED' AND (cast(:clinicId as Long) IS NULL OR a.clinic.id = :clinicId) GROUP BY a.patient.name HAVING COUNT(a) >= 2 ORDER BY COUNT(a) DESC")
    List<Object[]> getPatientsWithRepeatedCancellations(@Param("clinicId") Long clinicId);

    @Query("SELECT a.patient.name, COUNT(a) FROM Appointment a WHERE a.status = 'NO_SHOW' AND (cast(:clinicId as Long) IS NULL OR a.clinic.id = :clinicId) GROUP BY a.patient.name HAVING COUNT(a) >= 2 ORDER BY COUNT(a) DESC")
    List<Object[]> getPatientsWithRepeatedNoShows(@Param("clinicId") Long clinicId);

    @Query("SELECT p.gender, COUNT(p) FROM Patient p WHERE p.gender IS NOT NULL AND (cast(:clinicId as Long) IS NULL OR p.clinic.id = :clinicId) GROUP BY p.gender")
    List<Object[]> getGenderDistribution(@Param("clinicId") Long clinicId);

    @Query("SELECT p.dateOfBirth FROM Patient p WHERE p.dateOfBirth IS NOT NULL AND (cast(:clinicId as Long) IS NULL OR p.clinic.id = :clinicId)")
    List<java.time.LocalDate> getAllPatientBirthdates(@Param("clinicId") Long clinicId);
}

/*
 * This file contains the AnalyticsRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
