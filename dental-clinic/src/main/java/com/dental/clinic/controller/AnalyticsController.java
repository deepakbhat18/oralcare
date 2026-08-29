package com.dental.clinic.controller;

import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard analytics and reporting data")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get practice analytics summary stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getSummary()));
    }

    @GetMapping("/revenue/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Get monthly billing revenue breakdown as list of objects")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMonthlyRevenueData() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getMonthlyRevenueData()));
    }

    @GetMapping("/appointments/daily")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get daily appointment counts for last 7 days")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyAppointments() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDailyAppointments()));
    }

    @GetMapping("/treatments/distribution")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Get count distribution of treatment types across completed appointments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTreatmentDistribution() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTreatmentDistribution()));
    }

    @GetMapping("/queue/trend")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get average wait time trend for last 7 days")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getQueueTrend() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getQueueTrend()));
    }

    @GetMapping("/dentist/{dentistId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get summary stats for a specific dentist")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDentistSummary(@PathVariable Long dentistId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDentistSummary(dentistId)));
    }

    @GetMapping("/monthly-revenue")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Get monthly billing revenue breakdown for last 6 months")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getMonthlyRevenue() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getMonthlyRevenue()));
    }

    @GetMapping("/dentist-performance")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Get dentist productivity/revenue generated reports")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getRevenueByDentist() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getRevenueByDentist()));
    }

    @GetMapping("/appointment-stats")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Get counts and stats for appointments (cancellation rates, status counts)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAppointmentStats() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAppointmentStats()));
    }

    @GetMapping("/no-shows")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get no-show statistics and trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNoShowStats() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getNoShowStats()));
    }

    @GetMapping("/treatment-distribution-map")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Get count distribution of treatment types across completed appointments (Map)")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTreatmentTypeStats() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTreatmentTypeStats()));
    }

    @PostMapping("/clear-cache")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually evict/clear all cached reports")
    public ResponseEntity<ApiResponse<Void>> clearCache() {
        analyticsService.evictAnalyticsCache();
        return ResponseEntity.ok(ApiResponse.success("Analytics cache cleared successfully", null));
    }

    @GetMapping("/dashboard-summary")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get unified practice analytics dashboard summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary(
            @RequestParam(required = false) Long dentistId,
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboardSummary(dentistId, clinicId, startDate, endDate)));
    }

    @GetMapping("/wait-time")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get detailed wait time analytics and trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWaitTimeAnalytics(
            @RequestParam(required = false) Long dentistId,
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getWaitTimeAnalytics(dentistId, clinicId, startDate, endDate)));
    }

    @GetMapping("/treatments")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Get treatment distribution and revenue analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTreatmentAnalytics(
            @RequestParam(required = false) Long dentistId,
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTreatmentAnalytics(dentistId, clinicId, startDate, endDate)));
    }

    @GetMapping("/appointments-smart")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get advanced appointment booking and growth trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAppointmentAnalytics(
            @RequestParam(required = false) Long dentistId,
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAppointmentAnalytics(dentistId, clinicId, startDate, endDate)));
    }

    @GetMapping("/patients")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get patient demographic and behavioral analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPatientAnalytics(
            @RequestParam(required = false) Long clinicId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getPatientAnalytics(clinicId)));
    }

    @GetMapping("/dentists-smart")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    @Operation(summary = "Get detailed productivity and wait time reports per dentist")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDentistPerformance(
            @RequestParam(required = false) Long clinicId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDentistPerformance(clinicId)));
    }

    @GetMapping("/financials")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST')")
    @Operation(summary = "Get financial performance and billing analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFinancialAnalytics(
            @RequestParam(required = false) Long dentistId,
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getFinancialAnalytics(dentistId, clinicId, startDate, endDate)));
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get current queue metrics and utilization analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQueueAnalytics(
            @RequestParam(required = false) Long dentistId,
            @RequestParam(required = false) Long clinicId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getQueueAnalytics(dentistId, clinicId)));
    }

    @GetMapping("/business-insights")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get structured smart business insights for decision-making")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSmartBusinessInsights(
            @RequestParam(required = false) Long dentistId,
            @RequestParam(required = false) Long clinicId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getSmartBusinessInsights(dentistId, clinicId)));
    }
}

/*
 * This file contains the AnalyticsController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
