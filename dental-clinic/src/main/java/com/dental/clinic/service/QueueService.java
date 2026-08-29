package com.dental.clinic.service;

import com.dental.clinic.dto.response.AppointmentResponse;
import com.dental.clinic.dto.response.QueueStatusResponse;
import com.dental.clinic.exception.BadRequestException;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.Appointment;
import com.dental.clinic.model.Appointment.AppointmentStatus;
import com.dental.clinic.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final com.dental.clinic.scheduler.AppointmentScheduler appointmentScheduler;

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public AppointmentResponse checkIn(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException("Appointment cannot be checked in. Current status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.WAITING);
        appointment.setCheckInTime(LocalDateTime.now());
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Patient check-in successful. Appointment ID: {}", appointmentId);
        return appointmentService.mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public AppointmentResponse serve(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (appointment.getStatus() != AppointmentStatus.WAITING) {
            throw new BadRequestException("Only checked-in (WAITING) patients can be served. Current status: " + appointment.getStatus());
        }

        List<Appointment> active = appointmentRepository.findByDentistIdAndDateTimeBetween(
                appointment.getDentist().getId(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(23, 59, 59)
        ).stream()
         .filter(a -> a.getStatus() == AppointmentStatus.IN_PROGRESS)
         .collect(Collectors.toList());

        for (Appointment act : active) {
            appointmentService.updateStatus(act.getId(), AppointmentStatus.COMPLETED);
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment.setTreatmentStartTime(LocalDateTime.now());
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Patient served. Appointment ID: {}", appointmentId);
        return appointmentService.mapToResponse(saved);
    }

    @Transactional
    public QueueStatusResponse getQueueStatus(Long dentistId) {
        appointmentScheduler.processNoShows();

        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime now = LocalDateTime.now();

        List<Appointment> todayApts = appointmentRepository.findByDentistIdAndDateTimeBetween(dentistId, dayStart, dayEnd)
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.NO_SHOW && a.getStatus() != AppointmentStatus.COMPLETED)
                .collect(Collectors.toList());

        Appointment active = todayApts.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);

        List<AppointmentResponse> waitingList = todayApts.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.WAITING || 
                        ((a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.CONFIRMED) && !a.getDateTime().isBefore(now)))
                .sorted((a1, a2) -> {
                    if (a1.getQueuePosition() != null && a2.getQueuePosition() != null) {
                        return a1.getQueuePosition().compareTo(a2.getQueuePosition());
                    }
                    return a1.getDateTime().compareTo(a2.getDateTime());
                })
                .map(appointmentService::mapToResponse)
                .collect(Collectors.toList());

        AppointmentResponse activeResponse = active != null ? appointmentService.mapToResponse(active) : null;

        int totalWaiting = waitingList.size();
        int totalEstimatedWaitTime = waitingList.stream()
                .mapToInt(a -> a.getDurationMinutes() != null ? a.getDurationMinutes() : 30)
                .sum();

        return QueueStatusResponse.builder()
                .dentistId(dentistId)
                .activeAppointment(activeResponse)
                .waitingQueue(waitingList)
                .totalWaiting(totalWaiting)
                .totalEstimatedWaitTimeMinutes(totalEstimatedWaitTime)
                .build();
    }
}

/*
 * This file contains the QueueService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
