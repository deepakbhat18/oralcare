package com.dental.clinic.service;

import com.dental.clinic.exception.BadRequestException;
import com.dental.clinic.model.Appointment;
import com.dental.clinic.model.Patient;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.*;
import com.dental.clinic.scheduler.AppointmentScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AppointmentNoShowServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClinicRepository clinicRepository;
    @Mock private NotificationService notificationService;
    @Mock private DentistScheduleService dentistScheduleService;
    @Mock private MetricsService metricsService;
    @Mock private BillService billService;
    @Mock private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentScheduler appointmentScheduler;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSchedulerProcessesNoShows() {
        // Arrange
        User patientUser = User.builder().id(10L).email("patient@test.com").build();
        Patient patient = Patient.builder().id(1L).name("John Doe").user(patientUser).build();
        User dentist = User.builder().id(2L).name("Dr. Smith").role(User.Role.DENTIST).build();

        Appointment scheduledApt = Appointment.builder()
                .id(100L)
                .patient(patient)
                .dentist(dentist)
                .dateTime(LocalDateTime.now().minusHours(1))
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .build();

        when(appointmentRepository.findByStatusAndDateTimeBefore(eq(Appointment.AppointmentStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of(scheduledApt));
        when(appointmentRepository.findByStatusAndDateTimeBefore(eq(Appointment.AppointmentStatus.CONFIRMED), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        User admin = User.builder().id(5L).email("admin@test.com").role(User.Role.ADMIN).build();
        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(new ArrayList<>(List.of(admin)));
        when(userRepository.findByRole(User.Role.RECEPTIONIST)).thenReturn(new ArrayList<>());

        // Act
        appointmentScheduler.processNoShows();

        // Assert
        assertEquals(Appointment.AppointmentStatus.NO_SHOW, scheduledApt.getStatus());
        verify(appointmentRepository, times(1)).save(scheduledApt);
        verify(auditLogRepository, atLeastOnce()).save(any());
        verify(notificationService, times(2)).createNotification(any(), any(), any(), any(), any());
    }

    @Test
    public void testCannotTransitionFromNoShowToCompleted() {
        // Arrange
        Appointment apt = Appointment.builder()
                .id(1L)
                .status(Appointment.AppointmentStatus.NO_SHOW)
                .build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(apt));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            appointmentService.updateStatus(1L, Appointment.AppointmentStatus.COMPLETED);
        });
        assertTrue(ex.getMessage().contains("A NO_SHOW appointment cannot be updated to another status"));
    }

    @Test
    public void testCannotTransitionToNoShowFromCompleted() {
        // Arrange
        Appointment apt = Appointment.builder()
                .id(1L)
                .status(Appointment.AppointmentStatus.COMPLETED)
                .build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(apt));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            appointmentService.updateStatus(1L, Appointment.AppointmentStatus.NO_SHOW);
        });
        assertTrue(ex.getMessage().contains("Only SCHEDULED or CONFIRMED appointments can be marked as NO_SHOW"));
    }
}
