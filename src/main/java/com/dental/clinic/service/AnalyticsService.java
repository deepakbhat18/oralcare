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
import java.util.ArrayList;
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
        double noShowRate = total > 0 ? ((double) noShow / total) * 100 : 0.0;

        stats.put("totalAppointments", total);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);
        stats.put("noShow", noShow);
        stats.put("scheduled", scheduled);
        stats.put("cancellationRatePercent", Math.round(cancellationRate * 100.0) / 100.0);
        stats.put("noShowRatePercent", Math.round(noShowRate * 100.0) / 100.0);

        return stats;
    }

    @Cacheable(value = "analytics", key = "'no_show_stats'")
    @Transactional(readOnly = true)
    public Map<String, Object> getNoShowStats() {
        log.info("Cache MISS - Aggregating no-show stats");
        Map<String, Object> report = new HashMap<>();

        long totalAppointments = appointmentRepository.count();
        long totalNoShows = appointmentRepository.countByStatus(Appointment.AppointmentStatus.NO_SHOW);
        double noShowPercentage = totalAppointments > 0 ? ((double) totalNoShows / totalAppointments) * 100 : 0.0;

        report.put("totalNoShows", totalNoShows);
        report.put("noShowPercentage", Math.round(noShowPercentage * 100.0) / 100.0);

        // Most Frequently Missed Patients
        List<Object[]> missedPatientsRaw = appointmentRepository.findMostFrequentlyMissedPatients(org.springframework.data.domain.PageRequest.of(0, 5));
        List<Map<String, Object>> missedPatients = missedPatientsRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("patientName", row[0]);
            m.put("missedCount", row[1]);
            return m;
        }).collect(Collectors.toList());
        report.put("mostFrequentlyMissedPatients", missedPatients);

        // No-Show Rate by Dentist
        List<Object[]> dentistNoShowsRaw = appointmentRepository.findNoShowCountByDentist();
        Map<String, Long> noShowCounts = dentistNoShowsRaw.stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1],
                (a, b) -> a
        ));

        List<User> dentists = userRepository.findByRole(User.Role.DENTIST);
        Map<String, Double> noShowRateByDentist = new HashMap<>();
        for (User d : dentists) {
            long totalForDentist = appointmentRepository.findByDentistId(d.getId()).size();
            long noShowsForDentist = noShowCounts.getOrDefault(d.getName(), 0L);
            double rate = totalForDentist > 0 ? ((double) noShowsForDentist / totalForDentist) * 100 : 0.0;
            noShowRateByDentist.put(d.getName(), Math.round(rate * 100.0) / 100.0);
        }
        report.put("noShowRateByDentist", noShowRateByDentist);

        // Daily No-Show Trend (last 7 days)
        List<Map<String, Object>> dailyTrend = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(6).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<Object[]> dailyRaw = appointmentRepository.findDailyNoShowsInRange(start, end);
        
        Map<String, Long> dailyMap = new HashMap<>();
        for (Object[] row : dailyRaw) {
            if (row[0] != null && row[1] != null) {
                dailyMap.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> dayInfo = new HashMap<>();
            dayInfo.put("day", date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")));
            dayInfo.put("noShows", dailyMap.getOrDefault(date.toString(), 0L));
            dailyTrend.add(dayInfo);
        }
        report.put("dailyNoShowTrend", dailyTrend);

        // Monthly No-Show Trend (last 6 months)
        List<Map<String, Object>> monthlyTrend = new java.util.ArrayList<>();
        LocalDateTime sixMonthsAgo = today.minusMonths(5).withDayOfMonth(1).atStartOfDay();
        List<Object[]> monthlyRaw = appointmentRepository.findMonthlyNoShowsSince(sixMonthsAgo);
        
        Map<String, Long> monthlyMap = new HashMap<>();
        for (Object[] row : monthlyRaw) {
            if (row[0] != null && row[1] != null) {
                monthlyMap.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }
        
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.from(today.minusMonths(i));
            Map<String, Object> monthInfo = new HashMap<>();
            monthInfo.put("month", month.toString());
            monthInfo.put("noShows", monthlyMap.getOrDefault(month.toString(), 0L));
            monthlyTrend.add(monthInfo);
        }
        report.put("monthlyNoShowTrend", monthlyTrend);

        return report;
    }

    @Cacheable(value = "analytics", key = "'revenue_by_treatment'")
    @Transactional(readOnly = true)
    public Map<String, Long> getTreatmentTypeStats() {
        log.info("Cache MISS - Aggregating treatment distribution");
        List<Appointment> completed = appointmentRepository.findByStatus(Appointment.AppointmentStatus.COMPLETED);

        return completed.stream()
                .collect(Collectors.groupingBy(Appointment::getTreatmentType, Collectors.counting()));
    }

    @Cacheable(value = "smart-analytics", key = "'waitTime_' + #dentistId + '_' + #clinicId + '_' + #startDate + '_' + #endDate")
    @Transactional(readOnly = true)
    public Map<String, Object> getWaitTimeAnalytics(Long dentistId, Long clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Cache MISS - getWaitTimeAnalytics");
        Map<String, Object> data = new HashMap<>();

        Double avgWait = analyticsRepository.getActualAvgWaitTimeFiltered(dentistId, clinicId, startDate, endDate);
        if (avgWait == null || avgWait == 0.0) {
            Double estWait = analyticsRepository.avgQueueTimeInRange(startDate != null ? startDate : LocalDateTime.now().minusMonths(1), endDate != null ? endDate : LocalDateTime.now());
            avgWait = estWait != null ? estWait : 0.0;
        }
        Double maxWait = analyticsRepository.getActualMaxWaitTimeFiltered(dentistId, clinicId, startDate, endDate);
        Double minWait = analyticsRepository.getActualMinWaitTimeFiltered(dentistId, clinicId, startDate, endDate);

        data.put("averageWaitingTime", Math.round(avgWait * 100.0) / 100.0);
        data.put("maximumWaitingTime", Math.round((maxWait != null ? maxWait : 0.0) * 100.0) / 100.0);
        data.put("minimumWaitingTime", Math.round((minWait != null ? minWait : 0.0) * 100.0) / 100.0);

        long currentEstWait = appointmentRepository.findByStatus(Appointment.AppointmentStatus.WAITING).stream()
                .filter(a -> dentistId == null || a.getDentist().getId().equals(dentistId))
                .filter(a -> clinicId == null || (a.getClinic() != null && a.getClinic().getId().equals(clinicId)))
                .mapToLong(a -> a.getEstimatedWaitMinutes() != null ? a.getEstimatedWaitMinutes() : 0)
                .sum();
        data.put("currentEstimatedWaitingTime", currentEstWait);

        List<Object[]> dentistWaitRaw = analyticsRepository.getAvgWaitTimePerDentist();
        Map<String, Double> dentistWaitMap = new HashMap<>();
        for (Object[] row : dentistWaitRaw) {
            dentistWaitMap.put((String) row[0], Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0);
        }
        data.put("averageWaitingTimePerDentist", dentistWaitMap);

        List<Object[]> clinicWaitRaw = analyticsRepository.getAvgWaitTimePerClinic();
        Map<String, Double> clinicWaitMap = new HashMap<>();
        for (Object[] row : clinicWaitRaw) {
            clinicWaitMap.put((String) row[0], Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0);
        }
        data.put("averageWaitingTimePerClinic", clinicWaitMap);

        List<Object[]> dailyTrendRaw = analyticsRepository.getDailyWaitTimeTrend(dentistId, clinicId, startDate, endDate);
        List<Map<String, Object>> dailyTrend = dailyTrendRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("date", row[0].toString());
            m.put("avgWaitTime", Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0);
            return m;
        }).collect(Collectors.toList());
        data.put("dailyWaitTimeTrend", dailyTrend);

        List<Object[]> monthlyTrendRaw = analyticsRepository.getMonthlyWaitTimeTrend(dentistId, clinicId, startDate != null ? startDate : LocalDateTime.now().minusYears(1));
        List<Map<String, Object>> monthlyTrend = monthlyTrendRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("month", row[0].toString());
            m.put("avgWaitTime", Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0);
            return m;
        }).collect(Collectors.toList());
        data.put("monthlyWaitTimeTrend", monthlyTrend);

        return data;
    }

    @Cacheable(value = "smart-analytics", key = "'treatment_' + #dentistId + '_' + #clinicId + '_' + #startDate + '_' + #endDate")
    @Transactional(readOnly = true)
    public Map<String, Object> getTreatmentAnalytics(Long dentistId, Long clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Cache MISS - getTreatmentAnalytics");
        Map<String, Object> data = new HashMap<>();

        List<Object[]> countsRaw = analyticsRepository.getTreatmentCounts(dentistId, clinicId, startDate, endDate);
        List<Map<String, Object>> treatmentCounts = countsRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("treatmentType", row[0]);
            m.put("count", row[1]);
            return m;
        }).collect(Collectors.toList());
        
        data.put("treatmentCounts", treatmentCounts);
        data.put("mostCommonTreatment", treatmentCounts.isEmpty() ? null : treatmentCounts.get(0));
        data.put("leastCommonTreatment", treatmentCounts.isEmpty() ? null : treatmentCounts.get(treatmentCounts.size() - 1));

        List<Object[]> distMonthRaw = analyticsRepository.getTreatmentDistributionByMonth(dentistId, clinicId, startDate, endDate);
        List<Map<String, Object>> distByMonth = distMonthRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("month", row[0]);
            m.put("treatmentType", row[1]);
            m.put("count", row[2]);
            return m;
        }).collect(Collectors.toList());
        data.put("treatmentDistributionByMonth", distByMonth);

        List<Object[]> distDentistRaw = analyticsRepository.getTreatmentDistributionByDentist(clinicId, startDate, endDate);
        List<Map<String, Object>> distByDentist = distDentistRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("dentistName", row[0]);
            m.put("treatmentType", row[1]);
            m.put("count", row[2]);
            return m;
        }).collect(Collectors.toList());
        data.put("treatmentDistributionByDentist", distByDentist);

        long total = analyticsRepository.countAppointmentsFiltered(dentistId, clinicId, startDate, endDate);
        long completed = analyticsRepository.countAppointmentsByStatusFiltered(Appointment.AppointmentStatus.COMPLETED, dentistId, clinicId, startDate, endDate);
        long cancelled = analyticsRepository.countAppointmentsByStatusFiltered(Appointment.AppointmentStatus.CANCELLED, dentistId, clinicId, startDate, endDate);
        
        double completionRate = total > 0 ? ((double) completed / total) * 100 : 0.0;
        data.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        data.put("cancelledCount", cancelled);

        Double avgDuration = analyticsRepository.getAvgTreatmentDurationFiltered(dentistId, clinicId, startDate, endDate);
        data.put("averageTreatmentDurationMinutes", Math.round((avgDuration != null ? avgDuration : 0.0) * 100.0) / 100.0);

        List<Object[]> revPerTreatmentRaw = analyticsRepository.getRevenuePerTreatment(dentistId, clinicId);
        List<Map<String, Object>> revPerTreatment = revPerTreatmentRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("treatmentType", row[0]);
            m.put("revenue", Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0);
            return m;
        }).collect(Collectors.toList());
        data.put("revenuePerTreatment", revPerTreatment);

        return data;
    }

    @Cacheable(value = "smart-analytics", key = "'appointments_' + #dentistId + '_' + #clinicId + '_' + #startDate + '_' + #endDate")
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointmentAnalytics(Long dentistId, Long clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Cache MISS - getAppointmentAnalytics");
        Map<String, Object> data = new HashMap<>();

        long total = analyticsRepository.countAppointmentsFiltered(dentistId, clinicId, startDate, endDate);
        long completed = analyticsRepository.countAppointmentsByStatusFiltered(Appointment.AppointmentStatus.COMPLETED, dentistId, clinicId, startDate, endDate);
        long cancelled = analyticsRepository.countAppointmentsByStatusFiltered(Appointment.AppointmentStatus.CANCELLED, dentistId, clinicId, startDate, endDate);
        long noShow = analyticsRepository.countAppointmentsByStatusFiltered(Appointment.AppointmentStatus.NO_SHOW, dentistId, clinicId, startDate, endDate);
        long scheduled = analyticsRepository.countAppointmentsByStatusFiltered(Appointment.AppointmentStatus.SCHEDULED, dentistId, clinicId, startDate, endDate);
        long confirmed = analyticsRepository.countAppointmentsByStatusFiltered(Appointment.AppointmentStatus.CONFIRMED, dentistId, clinicId, startDate, endDate);

        data.put("totalAppointments", total);
        data.put("completedAppointments", completed);
        data.put("cancelledAppointments", cancelled);
        data.put("noShowAppointments", noShow);
        data.put("upcomingAppointments", scheduled + confirmed);

        List<Object[]> hoursRaw = analyticsRepository.getPeakBookingHours(dentistId, clinicId);
        List<Map<String, Object>> peakHours = hoursRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("hour", ((Number) row[0]).intValue());
            m.put("count", row[1]);
            return m;
        }).collect(Collectors.toList());
        data.put("peakBookingHours", peakHours);

        List<Object[]> daysRaw = analyticsRepository.getPeakBookingDays(dentistId, clinicId);
        List<Map<String, Object>> peakDays = daysRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("dayOfWeek", java.time.DayOfWeek.of(((Number) row[0]).intValue() == 0 ? 7 : ((Number) row[0]).intValue()).name());
            m.put("count", row[1]);
            return m;
        }).collect(Collectors.toList());
        data.put("peakBookingDays", peakDays);

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);
        long countThisMonth = analyticsRepository.countAppointmentsFiltered(dentistId, clinicId, oneMonthAgo, LocalDateTime.now());
        long countLastMonth = analyticsRepository.countAppointmentsFiltered(dentistId, clinicId, twoMonthsAgo, oneMonthAgo);
        
        double growthRate = countLastMonth > 0 ? (((double) countThisMonth - countLastMonth) / countLastMonth) * 100 : 0.0;
        data.put("monthlyGrowthPercentage", Math.round(growthRate * 100.0) / 100.0);

        return data;
    }

    @Cacheable(value = "smart-analytics", key = "'patients_' + #clinicId")
    @Transactional(readOnly = true)
    public Map<String, Object> getPatientAnalytics(Long clinicId) {
        log.info("Cache MISS - getPatientAnalytics");
        Map<String, Object> data = new HashMap<>();

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long newPatients = analyticsRepository.countNewPatients(startOfMonth, clinicId);
        long returningPatients = analyticsRepository.countReturningPatients(clinicId);

        data.put("newPatientsThisMonth", newPatients);
        data.put("returningPatients", returningPatients);

        List<Object[]> freqPatientsRaw = analyticsRepository.getMostFrequentPatients(clinicId);
        List<Map<String, Object>> freqPatients = freqPatientsRaw.stream().limit(10).map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("patientName", row[0]);
            m.put("completedAppointments", row[1]);
            return m;
        }).collect(Collectors.toList());
        data.put("mostFrequentPatients", freqPatients);

        List<Object[]> repeatedCancellationsRaw = analyticsRepository.getPatientsWithRepeatedCancellations(clinicId);
        List<Map<String, Object>> repeatedCancellations = repeatedCancellationsRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("patientName", row[0]);
            m.put("cancellationsCount", row[1]);
            return m;
        }).collect(Collectors.toList());
        data.put("patientsWithRepeatedCancellations", repeatedCancellations);

        List<Object[]> repeatedNoShowsRaw = analyticsRepository.getPatientsWithRepeatedNoShows(clinicId);
        List<Map<String, Object>> repeatedNoShows = repeatedNoShowsRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("patientName", row[0]);
            m.put("noShowsCount", row[1]);
            return m;
        }).collect(Collectors.toList());
        data.put("patientsWithRepeatedNoShows", repeatedNoShows);

        List<Object[]> genderRaw = analyticsRepository.getGenderDistribution(clinicId);
        Map<String, Long> genderDist = new HashMap<>();
        for (Object[] row : genderRaw) {
            genderDist.put((String) row[0], ((Number) row[1]).longValue());
        }
        data.put("genderDistribution", genderDist);

        List<LocalDate> birthdates = analyticsRepository.getAllPatientBirthdates(clinicId);
        Map<String, Integer> ageDist = new java.util.LinkedHashMap<>();
        ageDist.put("Under 18", 0);
        ageDist.put("18-35", 0);
        ageDist.put("36-50", 0);
        ageDist.put("51-65", 0);
        ageDist.put("Over 65", 0);

        for (LocalDate dob : birthdates) {
            int age = java.time.Period.between(dob, LocalDate.now()).getYears();
            if (age < 18) ageDist.put("Under 18", ageDist.get("Under 18") + 1);
            else if (age <= 35) ageDist.put("18-35", ageDist.get("18-35") + 1);
            else if (age <= 50) ageDist.put("36-50", ageDist.get("36-50") + 1);
            else if (age <= 65) ageDist.put("51-65", ageDist.get("51-65") + 1);
            else ageDist.put("Over 65", ageDist.get("Over 65") + 1);
        }
        data.put("ageGroupDistribution", ageDist);

        return data;
    }

    @Cacheable(value = "smart-analytics", key = "'dentist_performance_' + #clinicId")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDentistPerformance(Long clinicId) {
        log.info("Cache MISS - getDentistPerformance");
        List<Object[]> raw = analyticsRepository.getDentistPerformanceStats(clinicId);
        return raw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("dentistId", row[0]);
            m.put("dentistName", row[1]);
            m.put("patientsTreated", row[2]);
            m.put("treatmentsCompleted", row[3]);
            m.put("averageAppointmentDurationMinutes", Math.round(((Number) row[4]).doubleValue() * 100.0) / 100.0);
            m.put("averageWaitingTimeMinutes", Math.round(((Number) row[5]).doubleValue() * 100.0) / 100.0);
            m.put("revenueGenerated", Math.round(((Number) row[6]).doubleValue() * 100.0) / 100.0);
            m.put("appointmentCompletionRate", Math.round(((Number) row[7]).doubleValue() * 100.0) / 100.0);
            m.put("cancellationRate", Math.round(((Number) row[8]).doubleValue() * 100.0) / 100.0);
            return m;
        }).collect(Collectors.toList());
    }

    @Cacheable(value = "smart-analytics", key = "'financials_' + #dentistId + '_' + #clinicId + '_' + #startDate + '_' + #endDate")
    @Transactional(readOnly = true)
    public Map<String, Object> getFinancialAnalytics(Long dentistId, Long clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Cache MISS - getFinancialAnalytics");
        Map<String, Object> data = new HashMap<>();

        Double dailyRevenue = billRepository.sumRevenueFiltered(dentistId, clinicId, LocalDateTime.now().minusHours(24), LocalDateTime.now());
        data.put("dailyRevenue", Math.round((dailyRevenue != null ? dailyRevenue : 0.0) * 100.0) / 100.0);

        Double weeklyRevenue = billRepository.sumRevenueFiltered(dentistId, clinicId, LocalDateTime.now().minusDays(7), LocalDateTime.now());
        data.put("weeklyRevenue", Math.round((weeklyRevenue != null ? weeklyRevenue : 0.0) * 100.0) / 100.0);

        Double monthlyRevenue = billRepository.sumRevenueFiltered(dentistId, clinicId, LocalDate.now().withDayOfMonth(1).atStartOfDay(), LocalDateTime.now());
        data.put("monthlyRevenue", Math.round((monthlyRevenue != null ? monthlyRevenue : 0.0) * 100.0) / 100.0);

        Double annualRevenue = billRepository.sumRevenueFiltered(dentistId, clinicId, LocalDate.now().withDayOfYear(1).atStartOfDay(), LocalDateTime.now());
        data.put("annualRevenue", Math.round((annualRevenue != null ? annualRevenue : 0.0) * 100.0) / 100.0);

        Double outstanding = billRepository.getOutstandingAmountFiltered(dentistId, clinicId);
        data.put("outstandingPayments", Math.round((outstanding != null ? outstanding : 0.0) * 100.0) / 100.0);

        long paidCount = billRepository.countInvoicesByStatusFiltered(com.dental.clinic.model.Bill.BillStatus.PAID, dentistId, clinicId);
        long pendingCount = billRepository.countInvoicesByStatusFiltered(com.dental.clinic.model.Bill.BillStatus.UNPAID, dentistId, clinicId) 
                + billRepository.countInvoicesByStatusFiltered(com.dental.clinic.model.Bill.BillStatus.PARTIALLY_PAID, dentistId, clinicId);
        data.put("paidInvoicesCount", paidCount);
        data.put("pendingInvoicesCount", pendingCount);

        List<Object[]> revPerTreatmentRaw = analyticsRepository.getRevenuePerTreatment(dentistId, clinicId);
        List<Map<String, Object>> revPerTreatment = revPerTreatmentRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("treatmentType", row[0]);
            m.put("revenue", Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0);
            return m;
        }).collect(Collectors.toList());
        data.put("revenueByTreatment", revPerTreatment);

        List<Object[]> revPerDentistRaw = billRepository.getRevenueByDentist();
        List<Map<String, Object>> revPerDentist = revPerDentistRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("dentistName", row[0]);
            m.put("revenue", Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0);
            return m;
        }).collect(Collectors.toList());
        data.put("revenueByDentist", revPerDentist);

        List<Object[]> revPerClinicRaw = billRepository.getRevenueByClinic();
        List<Map<String, Object>> revPerClinic = revPerClinicRaw.stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("clinicName", row[0]);
            m.put("revenue", Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0);
            return m;
        }).collect(Collectors.toList());
        data.put("revenueByClinic", revPerClinic);

        return data;
    }

    @Cacheable(value = "smart-analytics", key = "'queue_' + #dentistId + '_' + #clinicId")
    @Transactional(readOnly = true)
    public Map<String, Object> getQueueAnalytics(Long dentistId, Long clinicId) {
        log.info("Cache MISS - getQueueAnalytics");
        Map<String, Object> data = new HashMap<>();

        LocalDate today = LocalDate.now();

        long currentQueueSize = appointmentRepository.findAll().stream()
                .filter(a -> a.getDateTime().toLocalDate().equals(today))
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.WAITING || a.getStatus() == Appointment.AppointmentStatus.IN_PROGRESS)
                .filter(a -> dentistId == null || a.getDentist().getId().equals(dentistId))
                .filter(a -> clinicId == null || (a.getClinic() != null && a.getClinic().getId().equals(clinicId)))
                .count();
        data.put("currentQueueSize", currentQueueSize);

        long totalToday = appointmentRepository.findAll().stream()
                .filter(a -> a.getDateTime().toLocalDate().equals(today))
                .filter(a -> a.getStatus() != Appointment.AppointmentStatus.CANCELLED)
                .filter(a -> dentistId == null || a.getDentist().getId().equals(dentistId))
                .filter(a -> clinicId == null || (a.getClinic() != null && a.getClinic().getId().equals(clinicId)))
                .count();
        data.put("averageQueueLength", totalToday);
        data.put("maximumQueueLength", totalToday);

        Double avgService = analyticsRepository.getAvgTreatmentDurationFiltered(dentistId, clinicId, null, null);
        data.put("averageServiceTimeMinutes", Math.round((avgService != null ? avgService : 30.0) * 100.0) / 100.0);

        long activeDentists = dentistId != null ? 1 : userRepository.countByRole(User.Role.DENTIST);
        double totalAvailableMins = activeDentists * 480.0;
        double totalServedMins = appointmentRepository.findAll().stream()
                .filter(a -> a.getDateTime().toLocalDate().equals(today))
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .filter(a -> dentistId == null || a.getDentist().getId().equals(dentistId))
                .filter(a -> clinicId == null || (a.getClinic() != null && a.getClinic().getId().equals(clinicId)))
                .mapToLong(a -> a.getDurationMinutes() != null ? a.getDurationMinutes() : 30)
                .sum();
        double utilization = totalAvailableMins > 0 ? (totalServedMins / totalAvailableMins) * 100 : 0.0;
        data.put("queueUtilizationPercentage", Math.round(utilization * 100.0) / 100.0);

        List<Object[]> peakRaw = analyticsRepository.getPeakBookingHours(dentistId, clinicId);
        List<Map<String, Object>> peakHours = peakRaw.stream().limit(3).map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("hour", ((Number) row[0]).intValue());
            m.put("appointmentsCount", row[1]);
            return m;
        }).collect(Collectors.toList());
        data.put("peakQueueHours", peakHours);

        return data;
    }

    @Cacheable(value = "smart-analytics", key = "'dashboard_summary_' + #dentistId + '_' + #clinicId + '_' + #startDate + '_' + #endDate")
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardSummary(Long dentistId, Long clinicId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Cache MISS - getDashboardSummary");
        Map<String, Object> summary = new HashMap<>();

        Map<String, Object> waitTime = getWaitTimeAnalytics(dentistId, clinicId, startDate, endDate);
        Map<String, Object> appointments = getAppointmentAnalytics(dentistId, clinicId, startDate, endDate);
        Map<String, Object> financials = getFinancialAnalytics(dentistId, clinicId, startDate, endDate);

        summary.put("operationalSummary", Map.of(
                "totalAppointments", appointments.get("totalAppointments"),
                "upcomingAppointments", appointments.get("upcomingAppointments"),
                "completedAppointments", appointments.get("completedAppointments"),
                "averageWaitingTime", waitTime.get("averageWaitingTime")
        ));

        summary.put("businessSummary", Map.of(
                "monthlyRevenue", financials.get("monthlyRevenue"),
                "outstandingPayments", financials.get("outstandingPayments"),
                "pendingInvoicesCount", financials.get("pendingInvoicesCount")
        ));

        summary.put("performanceSummary", Map.of(
                "completionRate", appointments.get("completedAppointments"),
                "noShowRate", appointments.get("noShowAppointments")
        ));

        return summary;
    }

    @Cacheable(value = "smart-analytics", key = "'business_insights_' + #dentistId + '_' + #clinicId")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSmartBusinessInsights(Long dentistId, Long clinicId) {
        log.info("Cache MISS - getSmartBusinessInsights");
        List<Map<String, Object>> insights = new ArrayList<>();

        List<Object[]> peakHours = analyticsRepository.getPeakBookingHours(dentistId, clinicId);
        if (!peakHours.isEmpty()) {
            int hr = ((Number) peakHours.get(0)[0]).intValue();
            insights.add(Map.of(
                    "type", "PEAK_HOURS",
                    "title", "Busiest Time of Day",
                    "description", String.format("Peak patient bookings occur at %02d:00. Ensure receptionist coverage and optimal queue management during this hour.", hr),
                    "impactLevel", "MEDIUM"
            ));
        }

        List<Object[]> performance = analyticsRepository.getDentistPerformanceStats(clinicId);
        if (!performance.isEmpty()) {
            Object[] topDentist = null;
            Object[] slowestDentist = null;
            double maxWait = -1.0;
            long maxTreated = -1;

            for (Object[] row : performance) {
                long treated = ((Number) row[3]).longValue();
                double wait = ((Number) row[5]).doubleValue();
                if (treated > maxTreated) {
                    maxTreated = treated;
                    topDentist = row;
                }
                if (wait > maxWait) {
                    maxWait = wait;
                    slowestDentist = row;
                }
            }

            if (topDentist != null) {
                insights.add(Map.of(
                        "type", "BUSIEST_DENTIST",
                        "title", "Top Performing Dentist",
                        "description", String.format("Dentist %s has completed the highest number of treatments (%d completed). Share scheduling best practices clinic-wide.", topDentist[1], maxTreated),
                        "impactLevel", "HIGH"
                ));
            }

            if (slowestDentist != null && maxWait > 15.0) {
                insights.add(Map.of(
                        "type", "BOTTLENECK",
                        "title", "Dentist Queue Bottleneck",
                        "description", String.format("Dentist %s exhibits an average patient wait time of %.1f minutes. Consider adjusting appointment intervals or buffer slots.", slowestDentist[1], maxWait),
                        "impactLevel", "HIGH"
                ));
            }
        }

        Double outstanding = billRepository.getOutstandingAmountFiltered(dentistId, clinicId);
        if (outstanding != null && outstanding > 1000.0) {
            insights.add(Map.of(
                    "type", "REVENUE_LEAKAGE",
                    "title", "Outstanding Invoices Alert",
                    "description", String.format("Outstanding accounts receivable is currently ₹%.2f. Recommend launching automated reminders to patients.", outstanding),
                    "impactLevel", "HIGH"
            ));
        }

        List<Object[]> peakDays = analyticsRepository.getPeakBookingDays(dentistId, clinicId);
        if (!peakDays.isEmpty()) {
            int dow = ((Number) peakDays.get(0)[0]).intValue();
            String dayName = java.time.DayOfWeek.of(dow == 0 ? 7 : dow).name();
            insights.add(Map.of(
                    "type", "PEAK_DAYS",
                    "title", "Busiest Day of Week",
                    "description", String.format("Patient traffic is highest on %s. Plan administrative and clinical staffing levels accordingly.", dayName),
                    "impactLevel", "MEDIUM"
            ));
        }

        return insights;
    }

    @CacheEvict(value = {"analytics", "smart-analytics"}, allEntries = true)
    public void evictAnalyticsCache() {
        log.info("Analytics cache cleared due to updates");
    }
}

/*
 * This file contains the AnalyticsService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
