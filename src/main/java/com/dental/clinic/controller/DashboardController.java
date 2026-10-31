package com.dental.clinic.controller;

import com.dental.clinic.dto.response.ApiResponse;
import com.dental.clinic.dto.response.DashboardStatsResponse;
import com.dental.clinic.dto.response.PagedResponse;
import com.dental.clinic.model.AuditLog;
import com.dental.clinic.repository.AuditLogRepository;
import com.dental.clinic.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregate clinic statistics and audit logs")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService   dashboardService;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','DENTIST','RECEPTIONIST')")
    @Operation(summary = "Get clinic summary statistics (Redis cached — 5 min TTL)")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummaryStats()));
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get paginated audit trail (ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "20")         int size,
            @RequestParam(defaultValue = "timestamp")  String sortBy) {
        var pageable  = PageRequest.of(page, size, Sort.by(sortBy).descending());
        var resultPage = auditLogRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(resultPage)));
    }

    @GetMapping("/audit-logs/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs for a specific user (ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogsByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(auditLogRepository.findByUsername(username, pageable))));
    }

    @DeleteMapping("/cache/evict")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually evict the dashboard Redis cache (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> evictCache() {
        dashboardService.evictCache();
        return ResponseEntity.ok(ApiResponse.success("Dashboard cache evicted", null));
    }
}

/*
 * This file contains the DashboardController class which manages HTTP request routing, mapping endpoints, and returning REST API responses.
 * Concepts used: Spring REST controllers, request mapping annotations, and client-server data exchange.
 */
