package com.dental.clinic.service;

import com.dental.clinic.model.Appointment;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final BillRepository billRepository;
    private final AppointmentRepository appointmentRepository;
    private final TreatmentRecordRepository treatmentRecordRepository;
    private final UserRepository userRepository;
    private final AnalyticsRepository analyticsRepository;

    @Cacheable(value = "analytics", key = "'summary'")
    @Transactional(readOnly = true)
    public Map<String, Object> getSummary() {
        log.info("Cache MISS - Aggregating analytics summary");
        Map<String, Object> summary = new HashMap<>();

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusSeconds(1);

        Double monthlyRevenue = billRepository.sumRevenueInPeriod(startOfMonth, endOfMonth);
        summary.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0.0);

        long thisMonthAppointments = analyticsRepository.countAppointmentsSince(startOfMonth);
        summary.put("thisMonthAppointments", thisMonthAppointments);

        Double avgQueueTime = analyticsRepository.avgQueueTimeInRange(startOfMonth, endOfMonth);
        summary.put("avgQueueTimeMinutes", avgQueueTime != null ? avgQueueTime : 0.0);

        long totalPatients = userRepository.countByRole(User.Role.PATIENT);
        summary.put("totalPatients", totalPatients);

        long pendingBills = billRepository.countByStatus(com.dental.clinic.model.Bill.BillStatus.UNPAID);
        summary.put("pendingBills", pendingBills);

        return summary;
    }

    @Cacheable(value = "analytics", key = "'revenue_monthly_data'")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlyRevenueData() {
        log.info("Cache MISS - Aggregating monthly revenue for last 6 months");
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.from(now.minusMonths(i));
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

            Double revenue = billRepository.sumRevenueInPeriod(start, end);
            double revVal = revenue != null ? revenue : 0.0;

            Map<String, Object> item = new HashMap<>();
            item.put("month", month.toString());
            item.put("revenue", revVal);
            item.put("target", 100000.0);
            list.add(item);
        }
        return list;
    }

    @Cacheable(value = "analytics", key = "'appointments_daily'")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailyAppointments() {
        log.info("Cache MISS - Aggregating daily appointments for last 7 days");
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();

            List<Object[]> counts = analyticsRepository.dailyCountsInRange(start, end);
            long appointments = 0;
            long completed = 0;
            if (counts != null && !counts.isEmpty()) {
                Object[] row = counts.get(0);
                appointments = row[1] != null ? ((Number) row[1]).longValue() : 0;
                completed = row[2] != null ? ((Number) row[2]).longValue() : 0;
            }

            Map<String, Object> item = new HashMap<>();
            String dayLabel = date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"));
            item.put("day", dayLabel);
            item.put("appointments", appointments);
            item.put("completed", completed);
            list.add(item);
        }
        return list;
    }

    @Cacheable(value = "analytics", key = "'treatments_distribution'")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTreatmentDistribution() {
        log.info("Cache MISS - Aggregating treatment distribution list");
        Map<String, Long> stats = getTreatmentTypeStats();
        return stats.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Cacheable(value = "analytics", key = "'queue_trend'")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getQueueTrend() {
        log.info("Cache MISS - Aggregating queue wait time trend for last 7 days");
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();

            Double avgWait = analyticsRepository.avgQueueTimeInRange(start, end);
            double avgWaitVal = avgWait != null ? avgWait : 0.0;

            Map<String, Object> item = new HashMap<>();
            String dayLabel = date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"));
            item.put("day", dayLabel);
            item.put("avgWait", Math.round(avgWaitVal * 10.0) / 10.0);
            list.add(item);
        }
        return list;
    }

    @Cacheable(value = "analytics", key = "'dentist_summary'")
    @Transactional(readOnly = true)
    public Map<String, Object> getDentistSummary(Long dentistId) {
        log.info("Cache MISS - Aggregating dentist summary for id: {}", dentistId);
        Map<String, Object> summary = new HashMap<>();

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long thisMonth = analyticsRepository.countByDentistSince(dentistId, startOfMonth);
        long completed = analyticsRepository.countCompletedByDentist(dentistId);
        long total = analyticsRepository.countTotalByDentist(dentistId);

        summary.put("thisMonthAppointments", thisMonth);
        summary.put("completedAppointments", completed);
        summary.put("totalAppointments", total);

        return summary;
    }

    @Cacheable(value = "analytics", key = "'revenue_monthly'")
    @Transactional(readOnly = true)
    public Map<String, Double> getMonthlyRevenue() {
        log.info("Cache MISS - Aggregating monthly revenue map");
        Map<String, Double> report = new HashMap<>();
        LocalDate now = LocalDate.now();

        for (int i = 0; i < 6; i++) {
            YearMonth month = YearMonth.from(now.minusMonths(i));
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);

            Double revenue = billRepository.sumRevenueInPeriod(start, end);
            report.put(month.toString(), revenue != null ? revenue : 0.0);
        }
        return report;
    }

    @Cacheable(value = "analytics", key = "'revenue_by_dentist'")
    @Transactional(readOnly = true)
    public Map<String, Double> getRevenueByDentist() {
        log.info("Cache MISS - Aggregating revenue by dentist");
        Map<String, Double> report = new HashMap<>();
        List<User> dentists = userRepository.findByRole(User.Role.DENTIST);

        for (User dentist : dentists) {
            Double revenue = treatmentRecordRepository.sumRevenueByDentist(dentist.getId());
            report.put(dentist.getName(), revenue != null ? revenue : 0.0);
        }
        return report;
    }

    @Cacheable(value = "analytics", key = "'appointment_stats'")
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointmentStats() {
        log.info("Cache MISS - Aggregating appointment statistics");
        Map<String, Object> stats = new HashMap<>();

        long total = appointmentRepository.count();
        long completed = appointmentRepository.countCompleted();
        long cancelled = appointmentRepository.countByStatus(Appointment.AppointmentStatus.CANCELLED);
        long noShow = appointmentRepository.countByStatus(Appointment.AppointmentStatus.NO_SHOW);
        long scheduled = appointmentRepository.countByStatus(Appointment.AppointmentStatus.SCHEDULED);

        double cancellationRate = total > 0 ? ((double) cancelled / total) * 100 : 0.0;

        stats.put("totalAppointments", total);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);
        stats.put("noShow", noShow);
        stats.put("scheduled", scheduled);
        stats.put("cancellationRatePercent", Math.round(cancellationRate * 100.0) / 100.0);

        return stats;
    }

    @Cacheable(value = "analytics", key = "'revenue_by_treatment'")
    @Transactional(readOnly = true)
    public Map<String, Long> getTreatmentTypeStats() {
        log.info("Cache MISS - Aggregating treatment distribution");
        List<Appointment> completed = appointmentRepository.findByStatus(Appointment.AppointmentStatus.COMPLETED);

        return completed.stream()
                .collect(Collectors.groupingBy(Appointment::getTreatmentType, Collectors.counting()));
    }

    @CacheEvict(value = "analytics", allEntries = true)
    public void evictAnalyticsCache() {
        log.info("Analytics cache cleared due to updates");
    }
}

/*
 * This file contains the AnalyticsService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
