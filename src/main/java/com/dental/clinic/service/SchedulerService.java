package com.dental.clinic.service;

import com.dental.clinic.model.Appointment;
import com.dental.clinic.model.Notification;
import com.dental.clinic.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;












@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService   notificationService;

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void sendAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusHours(26);


        List<Appointment> upcoming = appointmentRepository
                .findByDateTimeBetween(now, windowEnd);

        if (upcoming.isEmpty()) {
            return;
        }

        log.info("Reminder scheduler: checking {} upcoming appointments for 24h/1h reminders", upcoming.size());

        for (Appointment apt : upcoming) {
            if (apt.getStatus() == Appointment.AppointmentStatus.CANCELLED
                    || apt.getStatus() == Appointment.AppointmentStatus.NO_SHOW
                    || apt.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
                continue;
            }

            try {
                Long patientUserId = apt.getPatient().getUser() != null ? apt.getPatient().getUser().getId() : null;
                if (patientUserId == null) {
                    continue;
                }


                if (apt.getDateTime().isAfter(now.plusHours(23)) && apt.getDateTime().isBefore(now.plusHours(25))) {
                    if (!apt.isReminder24hSent()) {
                        String msg = String.format(
                                "Reminder: You have an appointment in 24 hours (on %s) for %s. Room: %s",
                                apt.getDateTime().toLocalTime(), apt.getTreatmentType(),
                                apt.getRoomNumber() != null ? apt.getRoomNumber() : "TBD");

                        notificationService.send(
                                patientUserId,
                                "24h Appointment Reminder",
                                msg,
                                Notification.NotificationType.REMINDER,
                                apt.getId(),
                                "Appointment"
                        );

                        apt.setReminder24hSent(true);
                        appointmentRepository.save(apt);
                        log.info("Sent 24h reminder for appointment id={}", apt.getId());
                    }
                }


                if (apt.getDateTime().isAfter(now) && apt.getDateTime().isBefore(now.plusHours(2))) {
                    if (!apt.isReminder1hSent()) {
                        String msg = String.format(
                                "Reminder: You have an appointment in 1 hour (on %s) for %s. Room: %s",
                                apt.getDateTime().toLocalTime(), apt.getTreatmentType(),
                                apt.getRoomNumber() != null ? apt.getRoomNumber() : "TBD");

                        notificationService.send(
                                patientUserId,
                                "1h Appointment Reminder",
                                msg,
                                Notification.NotificationType.REMINDER,
                                apt.getId(),
                                "Appointment"
                        );

                        apt.setReminder1hSent(true);
                        appointmentRepository.save(apt);
                        log.info("Sent 1h reminder for appointment id={}", apt.getId());
                    }
                }

            } catch (Exception e) {
                log.error("Failed to send reminder for appointmentId={}: {}",
                        apt.getId(), e.getMessage());
            }
        }
    }


    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markNoShows() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        List<Appointment> scheduled = appointmentRepository
                .findByStatusAndDateTimeBefore(Appointment.AppointmentStatus.SCHEDULED, cutoff);

        scheduled.forEach(apt -> {
            apt.setStatus(Appointment.AppointmentStatus.NO_SHOW);
            appointmentRepository.save(apt);
            log.info("Auto-marked appointment {} as NO_SHOW", apt.getId());
        });
    }
}

/*
 * This file contains the SchedulerService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
