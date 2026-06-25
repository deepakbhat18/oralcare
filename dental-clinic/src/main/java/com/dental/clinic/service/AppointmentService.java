
package com.dental.clinic.service;

import com.dental.clinic.dto.request.AppointmentRequest;
import com.dental.clinic.dto.request.BillRequest;
import com.dental.clinic.dto.response.AppointmentResponse;
import com.dental.clinic.exception.*;
import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import com.dental.clinic.security.aspect.Audit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    @Autowired
    private BillService billService;

    private static final int AVG_CONSULTATION_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final NotificationService notificationService;
    private final DentistScheduleService dentistScheduleService;
    private final MetricsService metricsService;

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public AppointmentResponse getById(Long id) {
        return mapToResponse(findById(id));
    }

    public List<AppointmentResponse> getByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AppointmentResponse> getByDentist(Long dentistId) {
        return appointmentRepository.findByDentistId(dentistId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AppointmentResponse> getUpcoming() {
        return appointmentRepository.findUpcomingAppointments(LocalDateTime.now()).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AppointmentResponse> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDateTimeBetween(start, end).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AppointmentResponse> getLiveQueue(Long dentistId) {
        return appointmentRepository.findLiveQueue(dentistId, LocalDateTime.now()).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    @Audit(action = "BOOK_APPOINTMENT", entityType = "Appointment")
    @CacheEvict(value = "analytics", allEntries = true)
    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));
        User dentist = userRepository.findById(request.getDentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist", request.getDentistId()));

        if (dentist.getRole() != User.Role.DENTIST) {
            throw new BadRequestException("Specified user is not a dentist");
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        if (request.getDateTime().toLocalDate().isBefore(today)) {
            throw new BadRequestException("Appointment date cannot be in the past. Today's date is allowed.");
        }


        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : AVG_CONSULTATION_MINUTES;
        dentistScheduleService.validateDentistSchedule(dentist.getId(), request.getDateTime(), duration);


        validateSlotOverlap(dentist.getId(), request.getDateTime(), duration, null);

        LocalDateTime bookingTime = LocalDateTime.now();

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .dentist(dentist)
                .treatmentType(request.getTreatmentType())
                .dateTime(request.getDateTime())
                .status(request.getStatus() != null ? request.getStatus() : Appointment.AppointmentStatus.SCHEDULED)
                .notes(request.getNotes())
                .durationMinutes(duration)
                .roomNumber(request.getRoomNumber())
                .bookingTime(bookingTime)
                .build();

        if (request.getClinicId() != null) {
            clinicRepository.findById(request.getClinicId()).ifPresent(appointment::setClinic);
        }

        appointment = appointmentRepository.save(appointment);

        recalculateQueue(dentist.getId(), request.getDateTime());

        appointment = findById(appointment.getId());

        notificationService.createNotification(
                patient.getUser() != null ? patient.getUser().getId() : null,
                "Appointment booked for " + request.getTreatmentType()
                        + " on " + request.getDateTime()
                        + ". Queue position: #" + appointment.getQueuePosition()
                        + ". Est. wait: " + appointment.getEstimatedWaitMinutes() + " min.",
                "Appointment Confirmed",
                Notification.NotificationType.APPOINTMENT,
                appointment.getId());

        log.info("Appointment booked: id={}, patient={}, queue_pos={}, est_wait={}min",
                appointment.getId(), patient.getName(),
                appointment.getQueuePosition(), appointment.getEstimatedWaitMinutes());

        metricsService.incrementAppointmentBooked(appointment.getClinic() != null ? appointment.getClinic().getId().toString() : null);

        return mapToResponse(appointment);
    }

    @Transactional
    @Audit(action = "UPDATE_APPOINTMENT", entityType = "Appointment")
    @CacheEvict(value = "analytics", allEntries = true)
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        Appointment appointment = findById(id);


        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : AVG_CONSULTATION_MINUTES;
        dentistScheduleService.validateDentistSchedule(appointment.getDentist().getId(), request.getDateTime(), duration);


        validateSlotOverlap(appointment.getDentist().getId(), request.getDateTime(), duration, id);

        appointment.setTreatmentType(request.getTreatmentType());
        appointment.setDateTime(request.getDateTime());
        if (request.getStatus() != null)
            appointment.setStatus(request.getStatus());
        appointment.setNotes(request.getNotes());
        appointment.setDurationMinutes(duration);
        appointment.setRoomNumber(request.getRoomNumber());
        return mapToResponse(appointmentRepository.save(appointment));
    }

    private void validateSlotOverlap(Long dentistId, LocalDateTime dateTime, int durationMinutes, Long ignoreAppointmentId) {
        LocalDateTime dayStart = dateTime.toLocalDate().atTime(0, 0);
        LocalDateTime dayEnd = dateTime.toLocalDate().atTime(23, 59, 59);

        List<Appointment> booked = appointmentRepository
                .findByDentistIdAndDateTimeBetween(dentistId, dayStart, dayEnd)
                .stream()
                .filter(a -> (ignoreAppointmentId == null || !a.getId().equals(ignoreAppointmentId))
                        && a.getStatus() != Appointment.AppointmentStatus.CANCELLED
                        && a.getStatus() != Appointment.AppointmentStatus.NO_SHOW)
                .collect(Collectors.toList());

        LocalDateTime start = dateTime;
        LocalDateTime end = start.plusMinutes(durationMinutes);

        for (Appointment b : booked) {
            LocalDateTime bStart = b.getDateTime();
            LocalDateTime bEnd = bStart.plusMinutes(b.getDurationMinutes() != null ? b.getDurationMinutes() : AVG_CONSULTATION_MINUTES);

            if (start.isBefore(bEnd) && bStart.isBefore(end)) {
                List<String> freeSlots = getAvailableSlots(dentistId, dateTime.toLocalDate());
                String slotsMsg = freeSlots.isEmpty()
                    ? "There are no free slots left on this day."
                    : "Available slots on this day: " + String.join(", ", freeSlots);

                throw new BadRequestException("The dentist already has an appointment overlapping this slot: "
                        + bStart.toLocalTime() + " - " + bEnd.toLocalTime() + ". " + slotsMsg);
            }
        }
    }

    public List<String> getAvailableSlots(Long dentistId, java.time.LocalDate date) {
        java.time.LocalDateTime dayStart = date.atTime(9, 0);
        java.time.LocalDateTime dayEnd = date.atTime(17, 0);

        List<java.time.LocalDateTime> booked = appointmentRepository
                .findByDentistIdAndDateTimeBetween(dentistId, dayStart, dayEnd)
                .stream()
                .filter(a -> a.getStatus() != Appointment.AppointmentStatus.CANCELLED
                        && a.getStatus() != Appointment.AppointmentStatus.NO_SHOW)
                .map(Appointment::getDateTime)
                .collect(Collectors.toList());

        List<String> slots = new java.util.ArrayList<>();
        java.time.LocalDateTime cursor = dayStart;
        while (cursor.isBefore(dayEnd)) {
            final java.time.LocalDateTime slot = cursor;
            if (booked.stream().noneMatch(b -> b.equals(slot))) {
                slots.add(slot.toLocalTime().toString());
            }
            cursor = cursor.plusMinutes(30);
        }
        return slots;
    }

    @Transactional
    @Audit(action = "UPDATE_APPOINTMENT_STATUS", entityType = "Appointment")
    @CacheEvict(value = "analytics", allEntries = true)
    public AppointmentResponse updateStatus(Long id, Appointment.AppointmentStatus status) {
        Appointment appointment = findById(id);
        appointment.setStatus(status);
        Appointment saved = appointmentRepository.save(appointment);

        if (status == Appointment.AppointmentStatus.COMPLETED) {
            BillRequest autoB = new BillRequest();

            autoB.setPatientId(appointment.getPatient().getId());

            double estimatedAmount = appointment.getDurationMinutes() != null
                    ? appointment.getDurationMinutes() * 10.0
                    : 500.0;

            autoB.setAmount(estimatedAmount);
            autoB.setNotes("Auto-generated on appointment completion. Please verify amount.");
            autoB.setDueDate(LocalDateTime.now().plusDays(7));

            billService.createBill(autoB);
        }

        if (status == Appointment.AppointmentStatus.COMPLETED
                || status == Appointment.AppointmentStatus.CANCELLED
                || status == Appointment.AppointmentStatus.NO_SHOW) {
            recalculateQueue(appointment.getDentist().getId(), appointment.getDateTime());
        }

        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "analytics", allEntries = true)
    public void deleteAppointment(Long id) {
        Appointment apt = findById(id);
        Long dentistId = apt.getDentist().getId();
        LocalDateTime dateTime = apt.getDateTime();
        appointmentRepository.deleteById(id);
        recalculateQueue(dentistId, dateTime);
    }

    private void recalculateQueue(Long dentistId, LocalDateTime dateTime) {
        List<Appointment> queue = appointmentRepository.findQueueForDentistOnDate(dentistId, dateTime);
        int cumulativeWait = 0;

        for (int i = 0; i < queue.size(); i++) {
            Appointment apt = queue.get(i);

            apt.setQueuePosition(i + 1);
            apt.setEstimatedWaitMinutes(cumulativeWait);

            int slotDuration = (apt.getDurationMinutes() != null)
                    ? apt.getDurationMinutes()
                    : AVG_CONSULTATION_MINUTES;

            cumulativeWait += slotDuration;

            appointmentRepository.save(apt);
        }
    }

    private Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    public AppointmentResponse mapToResponse(Appointment a) {
        return AppointmentResponse.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getName())
                .patientEmail(a.getPatient().getEmail())
                .patientContact(a.getPatient().getContact())
                .dentistId(a.getDentist().getId())
                .dentistName(a.getDentist().getName())
                .treatmentType(a.getTreatmentType())
                .dateTime(a.getDateTime())
                .status(a.getStatus())
                .notes(a.getNotes())
                .durationMinutes(a.getDurationMinutes())
                .roomNumber(a.getRoomNumber())
                .bookingTime(a.getBookingTime())
                .queuePosition(a.getQueuePosition())
                .estimatedWaitMinutes(a.getEstimatedWaitMinutes())
                .createdAt(a.getCreatedAt())
                .build();
    }
}

/*
 * We updated the booking logic here to dynamically check the incoming request status (so walk-ins get set to WAITING immediately).
 * We also enhanced the collision check: if the selected slot is already booked, the system calls getAvailableSlots to find
 * all remaining open spots for that doctor on that date, and appends them to the error message.
 * Key concepts: Spring Boot service layer, data validation, dynamic status checks, and scheduling conflict resolution.
 */