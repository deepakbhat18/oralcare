package com.dental.clinic.repository;

import com.dental.clinic.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDentistId(Long dentistId);
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);
    List<Appointment> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Appointment> findByDentistIdAndDateTimeBetween(Long dentistId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.dateTime >= :now AND a.status = 'SCHEDULED' ORDER BY a.dateTime")
    List<Appointment> findUpcomingAppointments(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.dentist.id = :dentistId AND DATE(a.dateTime) = DATE(:date)")
    long countByDentistAndDate(@Param("dentistId") Long dentistId, @Param("date") LocalDateTime date);
}
