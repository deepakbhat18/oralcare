package com.dental.clinic.repository;
import com.dental.clinic.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientId(Long patientId);
    List<Prescription> findByPatientIdAndActive(Long patientId, boolean active);
    List<Prescription> findByPrescribedById(Long doctorId);
}
