package com.dental.clinic.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public void incrementAuthFailure(String reason) {
        meterRegistry.counter("auth.failures.total", "reason", reason).increment();
    }

    public void incrementAppointmentBooked(String clinicId) {
        meterRegistry.counter("appointments.booked.total", "clinic", clinicId != null ? clinicId : "default").increment();
    }
}

/*
 * This file contains the MetricsService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
