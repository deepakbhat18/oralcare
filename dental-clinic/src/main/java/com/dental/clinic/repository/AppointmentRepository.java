package com.dental.clinic.repository;

import com.dental.clinic.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDentistId(Long dentistId);
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);
    List<Appointment> findByClinicId(Long clinicId);
    List<Appointment> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findByDentistIdAndDateTimeBetween(Long dentistId,java.time.LocalDateTime start, java.time.LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.dateTime >= :now AND a.status NOT IN ('COMPLETED','CANCELLED','NO_SHOW') ORDER BY a.dateTime")
    List<Appointment> findUpcomingAppointments(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.dentist.id = :dentistId AND DATE(a.dateTime) = DATE(:date)")
    long countByDentistAndDate(@Param("dentistId") Long dentistId, @Param("date") LocalDateTime date);

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.dentist.id = :dentistId
          AND CAST(a.dateTime AS LocalDate) = CAST(:slotDate AS LocalDate)
          AND a.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED')
        ORDER BY a.bookingTime ASC
        """)
    List<Appointment> findQueueForDentistOnDate(
            @Param("dentistId") Long dentistId,
            @Param("slotDate") LocalDateTime slotDate);


    @Query("""
        SELECT COUNT(a) FROM Appointment a
        WHERE a.dentist.id = :dentistId
          AND CAST(a.dateTime AS LocalDate) = CAST(:slotDate AS LocalDate)
          AND a.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED')
          AND a.bookingTime < :bookingTime
        """)
    int countAhead(
            @Param("dentistId") Long dentistId,
            @Param("slotDate") LocalDateTime slotDate,
            @Param("bookingTime") LocalDateTime bookingTime);

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.dentist.id = :dentistId
          AND CAST(a.dateTime AS LocalDate) = CAST(:today AS LocalDate)
          AND a.status IN ('WAITING', 'IN_PROGRESS', 'SCHEDULED', 'CONFIRMED')
        ORDER BY a.queuePosition ASC NULLS LAST, a.bookingTime ASC
        """)
    List<Appointment> findLiveQueue(@Param("dentistId") Long dentistId, @Param("today") LocalDateTime today);


    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'COMPLETED'")
    long countCompleted();

    @Query("SELECT COUNT(a) FROM Appointment a WHERE CAST(a.dateTime AS LocalDate) = CAST(:today AS LocalDate)")
    long countToday(@Param("today") LocalDateTime today);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId AND a.status = 'COMPLETED'")
    int countCompletedByPatient(@Param("patientId") Long patientId);


    @Query("""
            SELECT a FROM Appointment a
            WHERE a.patient.id = :patientId
              AND a.status = 'COMPLETED'
            ORDER BY a.dateTime DESC
            LIMIT 1
            """)
    Optional<Appointment> findLastCompletedByPatient(@Param("patientId") Long patientId);

    @Query("""
            SELECT COUNT(a) FROM Appointment a
            WHERE a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS')
              AND a.dateTime >= :windowStart
              AND a.dateTime <= :windowEnd
            """)
    int countActiveQueueInWindow(@Param("windowStart") LocalDateTime windowStart,
                                 @Param("windowEnd") LocalDateTime windowEnd);

    long countByStatus(Appointment.AppointmentStatus status);

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.status IN ('SCHEDULED', 'CONFIRMED')
          AND a.reminderSent = false
          AND a.dateTime >= :start AND a.dateTime <= :end
        """)
    List<Appointment> findPendingReminders(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Appointment> findByStatusAndDateTimeBefore(Appointment.AppointmentStatus status, LocalDateTime dateTime);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.dateTime >= :now AND a.status NOT IN ('COMPLETED','CANCELLED','NO_SHOW')")
    long countUpcoming(@Param("now") LocalDateTime now);

    @Query("SELECT a.patient.name, COUNT(a) FROM Appointment a WHERE a.status = 'NO_SHOW' GROUP BY a.patient.name ORDER BY COUNT(a) DESC")
    List<Object[]> findMostFrequentlyMissedPatients(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT a.dentist.name, COUNT(a) FROM Appointment a WHERE a.status = 'NO_SHOW' GROUP BY a.dentist.name")
    List<Object[]> findNoShowCountByDentist();

    @Query("SELECT CAST(a.dateTime AS LocalDate) as date, COUNT(a) FROM Appointment a WHERE a.status = 'NO_SHOW' AND a.dateTime >= :start AND a.dateTime < :end GROUP BY CAST(a.dateTime AS LocalDate) ORDER BY date")
    List<Object[]> findDailyNoShowsInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('TO_CHAR', a.dateTime, 'YYYY-MM') as ym, COUNT(a) FROM Appointment a WHERE a.status = 'NO_SHOW' AND a.dateTime >= :start GROUP BY FUNCTION('TO_CHAR', a.dateTime, 'YYYY-MM') ORDER BY ym")
    List<Object[]> findMonthlyNoShowsSince(@Param("start") LocalDateTime start);

    org.springframework.data.domain.Page<Appointment> findByStatus(Appointment.AppointmentStatus status, org.springframework.data.domain.Pageable pageable);

    List<Appointment> findByPatientIdAndStatus(Long patientId, Appointment.AppointmentStatus status);

    List<Appointment> findByDentistIdAndStatus(Long dentistId, Appointment.AppointmentStatus status);

    List<Appointment> findByStatusAndDateTimeBetween(Appointment.AppointmentStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.status = 'NO_SHOW' AND (" +
           "LOWER(a.patient.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.dentist.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.notes) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.treatmentType) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ")")
    org.springframework.data.domain.Page<Appointment> searchMissedAppointments(@Param("query") String query, org.springframework.data.domain.Pageable pageable);
}

/*
 * This file contains the AppointmentRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
