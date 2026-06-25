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
}

/*
 * This file contains the AnalyticsController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
