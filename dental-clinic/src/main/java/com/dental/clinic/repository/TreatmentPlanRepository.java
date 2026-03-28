package com.dental.clinic.repository;
import com.dental.clinic.model.TreatmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Long> {
    List<TreatmentPlan> findByPatientId(Long patientId);
    List<TreatmentPlan> findByDentistId(Long dentistId);
    List<TreatmentPlan> findByStatus(TreatmentPlan.TreatmentStatus status);
    List<TreatmentPlan> findByPatientIdAndStatus(Long patientId, TreatmentPlan.TreatmentStatus status);
}
