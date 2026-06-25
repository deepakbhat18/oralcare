package com.dental.clinic.repository;

import com.dental.clinic.model.DentistAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DentistAvailabilityRepository extends JpaRepository<DentistAvailability, Long> {
    List<DentistAvailability> findByDentistId(Long dentistId);
    Optional<DentistAvailability> findByDentistIdAndDayOfWeek(Long dentistId, Integer dayOfWeek);
}

/*
 * This file contains the DentistAvailabilityRepository class which manages database query execution, abstracting SQL statements, and retrieving raw entity data.
 * Concepts used: Spring Data repositories, JpaRepository capabilities, and custom database queries.
 */
