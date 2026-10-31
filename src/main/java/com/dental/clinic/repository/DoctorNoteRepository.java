package com.dental.clinic.repository;
import com.dental.clinic.model.DoctorNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface DoctorNoteRepository extends JpaRepository<DoctorNote, Long> {
    List<DoctorNote> findByPatientId(Long patientId);
    List<DoctorNote> findByDoctorId(Long doctorId);
}

/*
 * This file contains the DoctorNoteRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
