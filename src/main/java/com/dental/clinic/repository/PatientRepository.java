package com.dental.clinic.repository;
import com.dental.clinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByClinicId(Long clinicId);
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Patient> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT p FROM Patient p WHERE " +
           "EXISTS (SELECT a FROM Appointment a WHERE a.patient = p AND a.dentist.id = :dentistId) OR " +
           "EXISTS (SELECT t FROM TreatmentPlan t WHERE t.patient = p AND t.dentist.id = :dentistId) OR " +
           "EXISTS (SELECT n FROM DoctorNote n WHERE n.patient = p AND n.doctor.id = :dentistId) OR " +
           "EXISTS (SELECT pr FROM Prescription pr WHERE pr.patient = p AND pr.prescribedBy.id = :dentistId)")
    List<Patient> findByDentistId(@Param("dentistId") Long dentistId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Patient p WHERE p.id = :patientId AND (" +
           "EXISTS (SELECT a FROM Appointment a WHERE a.patient = p AND a.dentist.id = :dentistId) OR " +
           "EXISTS (SELECT t FROM TreatmentPlan t WHERE t.patient = p AND t.dentist.id = :dentistId) OR " +
           "EXISTS (SELECT n FROM DoctorNote n WHERE n.patient = p AND n.doctor.id = :dentistId) OR " +
           "EXISTS (SELECT pr FROM Prescription pr WHERE pr.patient = p AND pr.prescribedBy.id = :dentistId))")
    boolean isPatientAssociatedWithDentist(@Param("patientId") Long patientId, @Param("dentistId") Long dentistId);

    @Query("SELECT DISTINCT p FROM Patient p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND (" +
           "EXISTS (SELECT a FROM Appointment a WHERE a.patient = p AND a.dentist.id = :dentistId) OR " +
           "EXISTS (SELECT t FROM TreatmentPlan t WHERE t.patient = p AND t.dentist.id = :dentistId) OR " +
           "EXISTS (SELECT n FROM DoctorNote n WHERE n.patient = p AND n.doctor.id = :dentistId) OR " +
           "EXISTS (SELECT pr FROM Prescription pr WHERE pr.patient = p AND pr.prescribedBy.id = :dentistId))")
    List<Patient> searchPatientsByDentist(@Param("name") String name, @Param("dentistId") Long dentistId);

    @Query("SELECT DISTINCT p FROM Patient p WHERE p.clinic.id = :clinicId AND (" +
           "EXISTS (SELECT a FROM Appointment a WHERE a.patient = p AND a.dentist.id = :dentistId) OR " +
           "EXISTS (SELECT t FROM TreatmentPlan t WHERE t.patient = p AND t.dentist.id = :dentistId) OR " +
           "EXISTS (SELECT n FROM DoctorNote n WHERE n.patient = p AND n.doctor.id = :dentistId) OR " +
           "EXISTS (SELECT pr FROM Prescription pr WHERE pr.patient = p AND pr.prescribedBy.id = :dentistId))")
    List<Patient> findPatientsByClinicAndDentist(@Param("clinicId") Long clinicId, @Param("dentistId") Long dentistId);
}


/*
 * This file contains the PatientRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
