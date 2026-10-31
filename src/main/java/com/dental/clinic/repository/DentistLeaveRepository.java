package com.dental.clinic.repository;

import com.dental.clinic.model.DentistLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DentistLeaveRepository extends JpaRepository<DentistLeave, Long> {
    List<DentistLeave> findByDentistId(Long dentistId);

    @Query("SELECT dl FROM DentistLeave dl WHERE dl.dentist.id = :dentistId AND dl.status = 'APPROVED' AND :date BETWEEN dl.startDate AND dl.endDate")
    List<DentistLeave> findApprovedLeaveOnDate(@Param("dentistId") Long dentistId, @Param("date") LocalDate date);
}

/*
 * This file contains the DentistLeaveRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
