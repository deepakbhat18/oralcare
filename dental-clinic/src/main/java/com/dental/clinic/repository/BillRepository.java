package com.dental.clinic.repository;
import com.dental.clinic.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPatientId(Long patientId);
    List<Bill> findByStatus(Bill.BillStatus status);
    Optional<Bill> findByInvoiceNumber(String invoiceNumber);
    List<Bill> findByPatientIdAndStatus(Long patientId, Bill.BillStatus status);
}
