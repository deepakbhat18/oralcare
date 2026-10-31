package com.dental.clinic.service;

import com.dental.clinic.dto.response.AppointmentResponse;
import com.dental.clinic.model.Appointment;
import com.dental.clinic.model.Patient;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.*;
import com.dental.clinic.scheduler.AppointmentScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AppointmentQueueManagementTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClinicRepository clinicRepository;
    @Mock private NotificationService notificationService;
    @Mock private DentistScheduleService dentistScheduleService;
    @Mock private MetricsService metricsService;
    @Mock private BillService billService;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AppointmentScheduler appointmentScheduler;

    @InjectMocks
    private AppointmentService appointmentService;

    private User dentist;
    private Patient patient;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        dentist = User.builder().id(10L).name("Dr. Scenario Dentist 1").role(User.Role.DENTIST).build();
        User patientUser = User.builder().id(20L).email("patient@test.com").build();
        patient = Patient.builder().id(1L).name("Test Patient").user(patientUser).build();
    }

    @Test
    @DisplayName("1. July 13 appointment viewed on August 29 -> not upcoming")
    public void testJuly13AppointmentNotUpcomingOnAug29() {
        LocalDateTime july13 = LocalDateTime.of(2026, 7, 13, 10, 30);
        Appointment oldApt = Appointment.builder()
                .id(101L)
                .patient(patient)
                .dentist(dentist)
                .dateTime(july13)
                .status(Appointment.AppointmentStatus.NO_SHOW)
                .build();

        LocalDateTime august29 = LocalDateTime.of(2026, 8, 29, 10, 0);

        when(appointmentRepository.findUpcomingAppointments(august29)).thenReturn(Collections.emptyList());

        List<AppointmentResponse> upcoming = appointmentService.getUpcoming();

        assertTrue(upcoming.isEmpty(), "July 13 appointment should not be returned in upcoming appointments on Aug 29");
    }

    @Test
    @DisplayName("2. Past CONFIRMED appointment -> automatically marked as NO_SHOW")
    public void testPastConfirmedAppointmentBecomesNoShow() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(2);
        Appointment pastConfirmedApt = Appointment.builder()
                .id(102L)
                .patient(patient)
                .dentist(dentist)
                .dateTime(pastTime)
                .status(Appointment.AppointmentStatus.CONFIRMED)
                .build();

        AppointmentScheduler scheduler = new AppointmentScheduler(
                appointmentRepository, auditLogRepository, notificationService, userRepository);

        when(appointmentRepository.findByStatusAndDateTimeBefore(eq(Appointment.AppointmentStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findByStatusAndDateTimeBefore(eq(Appointment.AppointmentStatus.CONFIRMED), any(LocalDateTime.class)))
                .thenReturn(List.of(pastConfirmedApt));
        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(new ArrayList<>());
        when(userRepository.findByRole(User.Role.RECEPTIONIST)).thenReturn(new ArrayList<>());

        scheduler.processNoShows();

        assertEquals(Appointment.AppointmentStatus.NO_SHOW, pastConfirmedApt.getStatus());
        assertNull(pastConfirmedApt.getQueuePosition());
        assertNull(pastConfirmedApt.getEstimatedWaitMinutes());
        verify(appointmentRepository, times(1)).save(pastConfirmedApt);
    }

    @Test
    @DisplayName("3. Past SCHEDULED appointment -> automatically marked as NO_SHOW")
    public void testPastScheduledAppointmentBecomesNoShow() {
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        Appointment pastScheduledApt = Appointment.builder()
                .id(103L)
                .patient(patient)
                .dentist(dentist)
                .dateTime(pastTime)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .build();

        AppointmentScheduler scheduler = new AppointmentScheduler(
                appointmentRepository, auditLogRepository, notificationService, userRepository);

        when(appointmentRepository.findByStatusAndDateTimeBefore(eq(Appointment.AppointmentStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(List.of(pastScheduledApt));
        when(appointmentRepository.findByStatusAndDateTimeBefore(eq(Appointment.AppointmentStatus.CONFIRMED), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(userRepository.findByRole(User.Role.ADMIN)).thenReturn(new ArrayList<>());
        when(userRepository.findByRole(User.Role.RECEPTIONIST)).thenReturn(new ArrayList<>());

        scheduler.processNoShows();

        assertEquals(Appointment.AppointmentStatus.NO_SHOW, pastScheduledApt.getStatus());
        assertNull(pastScheduledApt.getQueuePosition());
        assertNull(pastScheduledApt.getEstimatedWaitMinutes());
        verify(appointmentRepository, times(1)).save(pastScheduledApt);
    }

    @Test
    @DisplayName("4. COMPLETED appointment -> remains COMPLETED")
    public void testCompletedAppointmentRemainsCompleted() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(3);
        Appointment completedApt = Appointment.builder()
                .id(104L)
                .patient(patient)
                .dentist(dentist)
                .dateTime(pastTime)
                .status(Appointment.AppointmentStatus.COMPLETED)
                .build();

        AppointmentScheduler scheduler = new AppointmentScheduler(
                appointmentRepository, auditLogRepository, notificationService, userRepository);

        when(appointmentRepository.findByStatusAndDateTimeBefore(eq(Appointment.AppointmentStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findByStatusAndDateTimeBefore(eq(Appointment.AppointmentStatus.CONFIRMED), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        scheduler.processNoShows();

        assertEquals(Appointment.AppointmentStatus.COMPLETED, completedApt.getStatus());
        verify(appointmentRepository, never()).save(completedApt);
    }

    @Test
    @DisplayName("5. CANCELLED appointment -> remains CANCELLED")
    public void testCancelledAppointmentRemainsCancelled() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(3);
        Appointment cancelledApt = Appointment.builder()
                .id(105L)
                .patient(patient)
                .dentist(dentist)
                .dateTime(pastTime)
                .status(Appointment.AppointmentStatus.CANCELLED)
                .build();

        AppointmentScheduler scheduler = new AppointmentScheduler(
                appointmentRepository, auditLogRepository, notificationService, userRepository);

        when(appointmentRepository.findByStatusAndDateTimeBefore(eq(Appointment.AppointmentStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findByStatusAndDateTimeBefore(eq(Appointment.AppointmentStatus.CONFIRMED), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        scheduler.processNoShows();

        assertEquals(Appointment.AppointmentStatus.CANCELLED, cancelledApt.getStatus());
        verify(appointmentRepository, never()).save(cancelledApt);
    }

    @Test
    @DisplayName("6. Today's active appointment -> appears in today's queue")
    public void testTodayActiveAppointmentAppearsInLiveQueue() {
        LocalDateTime todayTime = LocalDateTime.now().plusHours(1);
        Appointment activeApt = Appointment.builder()
                .id(106L)
                .patient(patient)
                .dentist(dentist)
                .dateTime(todayTime)
                .status(Appointment.AppointmentStatus.WAITING)
                .queuePosition(1)
                .estimatedWaitMinutes(0)
                .build();

        when(appointmentRepository.findLiveQueue(eq(dentist.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of(activeApt));

        List<AppointmentResponse> liveQueue = appointmentService.getLiveQueue(dentist.getId());

        assertEquals(1, liveQueue.size());
        assertEquals(106L, liveQueue.get(0).getId());
        assertEquals(Appointment.AppointmentStatus.WAITING, liveQueue.get(0).getStatus());
    }

    @Test
    @DisplayName("7. Historical appointment -> does not affect queue wait time or position")
    public void testHistoricalAppointmentDoesNotAffectQueueWaitTime() {
        LocalDateTime July13 = LocalDateTime.of(2026, 7, 13, 10, 30);
        Appointment historicalApt = Appointment.builder()
                .id(107L)
                .patient(patient)
                .dentist(dentist)
                .dateTime(July13)
                .status(Appointment.AppointmentStatus.NO_SHOW)
                .queuePosition(4)
                .estimatedWaitMinutes(90)
                .build();

        AppointmentResponse resp = appointmentService.mapToResponse(historicalApt);

        assertNull(resp.getQueuePosition(), "Queue position should be null for historical/inactive appointment");
        assertNull(resp.getEstimatedWaitMinutes(), "Estimated wait minutes should be null for historical/inactive appointment");
    }

    @Test
    @DisplayName("8. Future appointment -> appears in upcoming appointments")
    public void testFutureAppointmentAppearsInUpcoming() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        Appointment futureApt = Appointment.builder()
                .id(108L)
                .patient(patient)
                .dentist(dentist)
                .dateTime(futureTime)
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .build();

        when(appointmentRepository.findUpcomingAppointments(any(LocalDateTime.class)))
                .thenReturn(List.of(futureApt));

        List<AppointmentResponse> upcoming = appointmentService.getUpcoming();

        assertEquals(1, upcoming.size());
        assertEquals(108L, upcoming.get(0).getId());
        assertEquals(Appointment.AppointmentStatus.SCHEDULED, upcoming.get(0).getStatus());
    }

    @Test
    @DisplayName("9. WALK_IN creation attempted by PATIENT role -> throws AccessDeniedException (403 Forbidden)")
    public void testWalkInCreationDeniedForPatientRole() {
        org.springframework.security.core.Authentication patientAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "patientUser", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PATIENT")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(patientAuth);

        try {
            com.dental.clinic.dto.request.AppointmentRequest req = new com.dental.clinic.dto.request.AppointmentRequest();
            req.setPatientId(1L);
            req.setDentistId(10L);
            req.setTreatmentType("Walk-in Consultation");
            req.setDateTime(LocalDateTime.now());
            req.setAppointmentType(Appointment.AppointmentType.WALK_IN);
            req.setStatus(Appointment.AppointmentStatus.WAITING);

            when(patientRepository.findById(1L)).thenReturn(java.util.Optional.of(patient));
            when(userRepository.findById(10L)).thenReturn(java.util.Optional.of(dentist));

            assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
                appointmentService.bookAppointment(req);
            });
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("10. WALK_IN creation attempted by RECEPTIONIST role -> succeeds")
    public void testWalkInCreationAllowedForReceptionistRole() {
        org.springframework.security.core.Authentication recepAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "recepUser", "pass", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_RECEPTIONIST")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(recepAuth);

        try {
            com.dental.clinic.dto.request.AppointmentRequest req = new com.dental.clinic.dto.request.AppointmentRequest();
            req.setPatientId(1L);
            req.setDentistId(10L);
            req.setTreatmentType("Walk-in Consultation");
            req.setDateTime(LocalDateTime.now());
            req.setAppointmentType(Appointment.AppointmentType.WALK_IN);
            req.setStatus(Appointment.AppointmentStatus.WAITING);

            when(patientRepository.findById(1L)).thenReturn(java.util.Optional.of(patient));
            when(userRepository.findById(10L)).thenReturn(java.util.Optional.of(dentist));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
                Appointment a = invocation.getArgument(0);
                a.setId(999L);
                return a;
            });
            when(appointmentRepository.findById(999L)).thenAnswer(inv -> {
                Appointment a = Appointment.builder()
                        .id(999L)
                        .patient(patient)
                        .dentist(dentist)
                        .treatmentType("Walk-in Consultation")
                        .dateTime(req.getDateTime())
                        .status(Appointment.AppointmentStatus.WAITING)
                        .appointmentType(Appointment.AppointmentType.WALK_IN)
                        .build();
                return java.util.Optional.of(a);
            });

            AppointmentResponse resp = appointmentService.bookAppointment(req);

            assertNotNull(resp);
            assertEquals(999L, resp.getId());
            assertEquals(Appointment.AppointmentType.WALK_IN, resp.getAppointmentType());
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
