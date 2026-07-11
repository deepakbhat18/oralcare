package com.dental.clinic.service;

import com.dental.clinic.dto.response.DashboardStatsResponse;
import com.dental.clinic.model.Appointment;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserRepository         userRepository;
    private final AppointmentRepository  appointmentRepository;
    private final DocumentRepository     documentRepository;
    private final NotificationRepository notificationRepository;
    private final BillRepository         billRepository;









    @Cacheable(value = "dashboardStats", key = "'summary'")
    @Transactional(readOnly = true)
    public DashboardStatsResponse getSummaryStats() {
        log.debug("Cache MISS — computing dashboard stats from DB");

        long patients    = userRepository.countByRole(User.Role.PATIENT);
        long doctors     = userRepository.countByRole(User.Role.DENTIST);
        long todayApts   = appointmentRepository.countToday(LocalDateTime.now());
        long completed   = appointmentRepository.countCompleted();
        long cancelled   = appointmentRepository.countByStatus(Appointment.AppointmentStatus.CANCELLED);
        long noShow      = appointmentRepository.countByStatus(Appointment.AppointmentStatus.NO_SHOW);
        long upcoming    = appointmentRepository.countUpcoming(LocalDateTime.now());
        long documents   = documentRepository.count();
        long unreadNotif = 0L;
        long pendingBill = billRepository.countByStatus(com.dental.clinic.model.Bill.BillStatus.UNPAID);

        return DashboardStatsResponse.builder()
                .totalPatients(patients)
                .totalDoctors(doctors)
                .totalAppointmentsToday(todayApts)
                .totalAppointmentsCompleted(completed)
                .totalAppointmentsCancelled(cancelled)
                .totalAppointmentsUpcoming(upcoming)
                .totalAppointmentsNoShow(noShow)
                .totalDocumentsUploaded(documents)
                .unreadNotifications(unreadNotif)
                .pendingBills(pendingBill)
                .build();
    }


    @CacheEvict(value = "dashboardStats", allEntries = true)
    public void evictCache() {
        log.debug("Dashboard cache evicted");
    }
}

/*
 * This file contains the DashboardService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
