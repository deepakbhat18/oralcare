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
}

/*
 * This file contains the AnalyticsRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
