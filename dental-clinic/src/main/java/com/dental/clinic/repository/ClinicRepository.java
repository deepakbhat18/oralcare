package com.dental.clinic.repository;
import com.dental.clinic.model.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    Optional<Clinic> findByTenantId(String tenantId);
    boolean existsByTenantId(String tenantId);
}
