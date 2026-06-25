package com.dental.clinic.repository;
import com.dental.clinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
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

}

/*
 * This file contains the PatientRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
