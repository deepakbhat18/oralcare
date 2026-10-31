package com.dental.clinic.scheduler;

import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import com.dental.clinic.service.EmailService;
import com.dental.clinic.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClinicScheduler {

    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;





    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendAppointmentReminders() {
        log.debug("Running appointment reminder scheduler...");
        LocalDateTime start = LocalDateTime.now().plusHours(23);
        LocalDateTime end = LocalDateTime.now().plusHours(25);

        List<Appointment> upcoming = appointmentRepository
                .findByDateTimeBetween(start, end)
                .stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED
                          || a.getStatus() == Appointment.AppointmentStatus.CONFIRMED)
                .toList();

        for (Appointment appt : upcoming) {
            try {
                Patient patient = appt.getPatient();
                if (patient.getEmail() != null) {
                    emailService.sendAppointmentReminder(
                            patient.getEmail(),
                            patient.getName(),
                            appt.getDateTime().toString(),
                            appt.getTreatmentType());
                }

                if (patient.getUser() != null) {
                    notificationService.createNotification(
                            patient.getUser().getId(),
                            "Reminder: Your " + appt.getTreatmentType() + " appointment is tomorrow at "
                                    + appt.getDateTime().toLocalTime(),
                            "Appointment Reminder",
                            Notification.NotificationType.REMINDER,
                            appt.getId());
                }
            } catch (Exception e) {
                log.error("Failed to send reminder for appointment {}: {}", appt.getId(), e.getMessage());
            }
        }

        if (!upcoming.isEmpty()) {
            log.info("Appointment reminders sent for {} appointments", upcoming.size());
        }
    }





    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markOverdueBills() {
        log.debug("Running overdue bills scheduler...");
        List<Bill> overdueBills = billRepository.findByStatus(Bill.BillStatus.UNPAID)
                .stream()
                .filter(b -> b.getDueDate() != null && b.getDueDate().isBefore(LocalDateTime.now()))
                .toList();

        for (Bill bill : overdueBills) {
            bill.setStatus(Bill.BillStatus.OVERDUE);
            billRepository.save(bill);


            Patient patient = bill.getPatient();
            if (patient.getUser() != null) {
                notificationService.createNotification(
                        patient.getUser().getId(),
                        "Your invoice " + bill.getInvoiceNumber() + " of ₹" + bill.getTotalAmount()
                                + " is overdue. Please clear the dues.",
                        "Overdue Bill Alert",
                        Notification.NotificationType.BILL,
                        bill.getId());
            }
        }

        if (!overdueBills.isEmpty()) {
            log.info("Marked {} bills as overdue", overdueBills.size());
        }
    }





    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        log.debug("Cleaning expired password reset tokens...");
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Expired password reset tokens cleaned up");
    }





    @Scheduled(cron = "0 0 9 * * MON")
    @Transactional
    public void sendFollowUpReminders() {
        log.debug("Running follow-up reminder scheduler...");
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        LocalDateTime sevenMonthsAgo = LocalDateTime.now().minusMonths(7);

        List<Appointment> oldCompleted = appointmentRepository
                .findByDateTimeBetween(sevenMonthsAgo, sixMonthsAgo)
                .stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .toList();

        for (Appointment appt : oldCompleted) {
            try {
                Patient patient = appt.getPatient();
                if (patient.getUser() != null) {
                    notificationService.createNotification(
                            patient.getUser().getId(),
                            "It's been 6 months since your last visit. Time for a routine dental check-up!",
                            "Time for a Check-up!",
                            Notification.NotificationType.REMINDER,
                            null);
                }
            } catch (Exception e) {
                log.warn("Failed to send follow-up for patient {}: {}", appt.getPatient().getId(), e.getMessage());
            }
        }

        log.info("Follow-up reminders sent for {} patients", oldCompleted.size());
    }
}

/*
 * This file contains the ClinicScheduler class which manages running scheduled cron jobs, background database maintenance, and queue cleanups.
 * Concepts used: Spring task scheduling, cron expression triggers, and asynchronous background jobs.
 */
