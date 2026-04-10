
package com.dental.clinic.repository;

import com.dental.clinic.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByPatientId(Long patientId);
    List<Report> findByPatientIdOrderByUploadedAtDesc(Long patientId);
    List<Report> findByPatientIdAndReportType(Long patientId, String reportType);
    List<Report> findByDoctorId(Long doctorId);
}