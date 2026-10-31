package com.dental.clinic.repository;

import com.dental.clinic.model.TreatmentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentRecordRepository extends JpaRepository<TreatmentRecord, Long> {
    Page<TreatmentRecord> findByPatientId(Long patientId, Pageable pageable);
    Page<TreatmentRecord> findByDentistId(Long dentistId, Pageable pageable);
    List<TreatmentRecord> findByPatientId(Long patientId);

    @Query("SELECT COALESCE(SUM(tr.cost), 0.0) FROM TreatmentRecord tr WHERE tr.dentist.id = :dentistId")
    Double sumRevenueByDentist(@Param("dentistId") Long dentistId);
}

/*
 * This file contains the TreatmentRecordRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
