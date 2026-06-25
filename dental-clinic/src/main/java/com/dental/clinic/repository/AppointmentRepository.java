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
}

/*
 * This file contains the AppointmentRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
