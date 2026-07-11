package com.dental.clinic.repository;

import com.dental.clinic.model.ToothRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToothRecordRepository extends JpaRepository<ToothRecord, Long> {
    List<ToothRecord> findByPatientId(Long patientId);
    Optional<ToothRecord> findByPatientIdAndToothNumber(Long patientId, Integer toothNumber);
}
