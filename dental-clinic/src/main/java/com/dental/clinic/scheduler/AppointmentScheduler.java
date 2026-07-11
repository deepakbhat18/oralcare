package com.dental.clinic.scheduler;

import com.dental.clinic.model.Appointment;
import com.dental.clinic.model.AuditLog;
import com.dental.clinic.model.Notification;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.AppointmentRepository;
import com.dental.clinic.repository.AuditLogRepository;
import com.dental.clinic.repository.UserRepository;
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
public class AppointmentScheduler {

    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 */15 * * * *") // Runs every 15 minutes
    @Transactional
    public void processNoShows() {
        log.info("Executing appointment no-show detection scheduler...");
        LocalDateTime now = LocalDateTime.now();

        // Save scheduler execution log
        AuditLog schedulerLog = AuditLog.builder()
                .username("SYSTEM_SCHEDULER")
                .action("SCHEDULER_EXECUTION")
                .entityType("AppointmentScheduler")
                .ipAddress("127.0.0.1")
                .extraInfo("Execution time: " + now)
                .timestamp(now)
                .build();
        auditLogRepository.save(schedulerLog);

        // Find all SCHEDULED or CONFIRMED appointments where dateTime is before now
        List<Appointment> scheduledApts = appointmentRepository.findByStatusAndDateTimeBefore(
                Appointment.AppointmentStatus.SCHEDULED, now);
        List<Appointment> confirmedApts = appointmentRepository.findByStatusAndDateTimeBefore(
                Appointment.AppointmentStatus.CONFIRMED, now);

        processList(scheduledApts, now);
        processList(confirmedApts, now);
    }

    private void processList(List<Appointment> appointments, LocalDateTime now) {
        if (appointments == null || appointments.isEmpty()) return;

        List<User> adminsAndReceptionists = userRepository.findByRole(User.Role.ADMIN);
        adminsAndReceptionists.addAll(userRepository.findByRole(User.Role.RECEPTIONIST));

        for (Appointment apt : appointments) {
            log.info("Marking appointment #{} for patient {} as NO_SHOW (scheduled time was {})",
                    apt.getId(), apt.getPatient().getName(), apt.getDateTime());

            // Update status to NO_SHOW
            apt.setStatus(Appointment.AppointmentStatus.NO_SHOW);
            appointmentRepository.save(apt);

            // Audit log for appointment status update
            AuditLog auditLog = AuditLog.builder()
                    .username("SYSTEM_SCHEDULER")
                    .action("APPOINTMENT_MARKED_NO_SHOW")
                    .entityType("Appointment")
                    .entityId(apt.getId())
                    .ipAddress("127.0.0.1")
                    .extraInfo("Automatically marked as NO_SHOW because visit time passed without check-in. Scheduled: " + apt.getDateTime())
                    .timestamp(now)
                    .build();
            auditLogRepository.save(auditLog);

            // Notify patient
            if (apt.getPatient().getUser() != null) {
                String patientMsg = "Your appointment on " + apt.getDateTime().toLocalDate() + 
                                    " was marked as missed because you did not attend the scheduled visit. " +
                                    "Please book a new appointment if you wish to continue treatment.";
                notificationService.createNotification(
                        apt.getPatient().getUser().getId(),
                        "Appointment Missed",
                        patientMsg,
                        Notification.NotificationType.APPOINTMENT,
                        apt.getId());
            }

            // Notify receptionists/admins
            String staffMsg = "Appointment #" + apt.getId() + " has been automatically marked as NO_SHOW.";
            for (User staff : adminsAndReceptionists) {
                notificationService.createNotification(
                        staff.getId(),
                        "Appointment No-Show Alert",
                        staffMsg,
                        Notification.NotificationType.APPOINTMENT,
                        apt.getId());
            }
        }
    }
}
