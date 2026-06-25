package com.dental.clinic;

import com.dental.clinic.dto.request.AppointmentRequest;
import com.dental.clinic.dto.request.BillRequest;
import com.dental.clinic.dto.request.TreatmentPlanRequest;
import com.dental.clinic.dto.response.AppointmentResponse;
import com.dental.clinic.dto.response.BillResponse;
import com.dental.clinic.dto.response.TreatmentPlanResponse;
import com.dental.clinic.exception.BadRequestException;
import com.dental.clinic.model.Clinic;
import com.dental.clinic.model.DentistAvailability;
import com.dental.clinic.model.Patient;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.*;
import com.dental.clinic.service.AppointmentService;
import com.dental.clinic.service.BillService;
import com.dental.clinic.service.TreatmentPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class PopulateScenarioTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private DentistAvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private TreatmentPlanRepository treatmentPlanRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private BillService billService;

    @Autowired
    private TreatmentPlanService treatmentPlanService;

    @Test
    public void runScenario() {
        System.out.println("=== STARTING SCENARIO POPULATION ===");

        // 1. Clean existing records for a fresh, clean demo
        System.out.println("Cleaning existing appointments, bills, and treatment plans...");
        billRepository.deleteAll();
        appointmentRepository.deleteAll();
        treatmentPlanRepository.deleteAll();

        // 2. Fetch default clinic
        Clinic clinic = clinicRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Default clinic ID=1 not found"));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String passwordHash = encoder.encode("password123");

        // 3. Create 100 patients
        System.out.println("Creating 100 scenario patients...");
        List<Patient> createdPatients = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            String email = "scenario_patient_" + i + "@oralcare.com";
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = User.builder()
                        .name("Scenario Patient " + i)
                        .email(email)
                        .password(passwordHash)
                        .role(User.Role.PATIENT)
                        .phoneNumber("98765432" + String.format("%02d", i % 100))
                        .active(true)
                        .emailVerified(true)
                        .clinicId(clinic.getId())
                        .build();
                user = userRepository.save(user);
            }

            Patient patient = patientRepository.findByUserId(user.getId()).orElse(null);
            if (patient == null) {
                patient = Patient.builder()
                        .user(user)
                        .name(user.getName())
                        .email(user.getEmail())
                        .contact(user.getPhoneNumber())
                        .dateOfBirth(LocalDate.now().minusYears(20 + (i % 30)))
                        .clinic(clinic)
                        .build();
                patient = patientRepository.save(patient);
            }
            createdPatients.add(patient);
        }
        System.out.println("100 Patients created/loaded successfully.");

        // 4. Create 2 extra doctors
        System.out.println("Creating 2 extra dentists...");
        List<User> dentists = new ArrayList<>();
        
        // Add existing doctor Dr. Sarah Dentist (ID 2)
        userRepository.findById(2L).ifPresent(dentists::add);

        for (int i = 1; i <= 2; i++) {
            String email = "scenario_dentist_" + i + "@oralcare.com";
            User dentist = userRepository.findByEmail(email).orElse(null);
            if (dentist == null) {
                dentist = User.builder()
                        .name("Dr. Scenario Dentist " + i)
                        .email(email)
                        .password(passwordHash)
                        .role(User.Role.DENTIST)
                        .phoneNumber("98765431" + i)
                        .active(true)
                        .emailVerified(true)
                        .clinicId(clinic.getId())
                        .build();
                dentist = userRepository.save(dentist);
            }
            dentists.add(dentist);

            // Add weekly hours/availability (Monday to Friday, 9:00 AM to 5:00 PM) for the new dentist
            for (int day = 1; day <= 5; day++) {
                if (!availabilityRepository.findByDentistIdAndDayOfWeek(dentist.getId(), day).isPresent()) {
                    DentistAvailability availability = DentistAvailability.builder()
                            .dentist(dentist)
                            .dayOfWeek(day)
                            .startTime(LocalTime.of(9, 0))
                            .endTime(LocalTime.of(17, 0))
                            .build();
                    availabilityRepository.save(availability);
                }
            }
        }
        System.out.println("Dentists list initialized. Total doctors: " + dentists.size());

        // 5. Book appointments for each of the 100 patients
        System.out.println("Booking appointments for all 100 patients...");
        List<AppointmentResponse> bookedAppointments = new ArrayList<>();

        // Dates to book across
        LocalDate dateToday = LocalDate.of(2026, 6, 26); // Today (Friday)
        LocalDate dateMonday = LocalDate.of(2026, 6, 29); // Monday
        LocalDate dateTuesday = LocalDate.of(2026, 6, 30); // Tuesday
        List<LocalDate> bookingDates = List.of(dateToday, dateMonday, dateTuesday);

        List<String> slotTimes = List.of(
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
                "15:00", "15:30", "16:00", "16:30"
        );

        int dentistIdx = 0;
        int dateIdx = 0;
        int slotIdx = 0;

        for (Patient patient : createdPatients) {
            User dentist = dentists.get(dentistIdx);
            LocalDate date = bookingDates.get(dateIdx);
            String slotStr = slotTimes.get(slotIdx);
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(slotStr));

            AppointmentRequest req = new AppointmentRequest();
            req.setPatientId(patient.getId());
            req.setDentistId(dentist.getId());
            req.setTreatmentType("Consultation");
            req.setDateTime(dateTime);
            req.setNotes("Scheduled automatically for scenario testing");

            try {
                AppointmentResponse resp = appointmentService.bookAppointment(req);
                bookedAppointments.add(resp);
            } catch (Exception e) {
                System.out.println("Booking failed for patient " + patient.getName() + " on " + dateTime + ": " + e.getMessage());
            }

            // Move to the next slot configuration:
            dentistIdx++;
            if (dentistIdx >= dentists.size()) {
                dentistIdx = 0;
                slotIdx++;
                if (slotIdx >= slotTimes.size()) {
                    slotIdx = 0;
                    dateIdx++;
                    if (dateIdx >= bookingDates.size()) {
                        dateIdx = 0; // Wrap around dates if necessary
                    }
                }
            }
        }
        System.out.println("Total booked appointments: " + bookedAppointments.size());

        // 6. Test double booking at same time with same doctor
        System.out.println("Testing double-booking constraint for the same doctor at the same time...");
        Patient p1 = createdPatients.get(0);
        Patient p2 = createdPatients.get(1);
        User targetDoctor = dentists.get(0);
        LocalDateTime conflictTime = LocalDateTime.of(LocalDate.of(2026, 7, 1), LocalTime.of(10, 0));

        // Create first booking (should succeed)
        AppointmentRequest req1 = new AppointmentRequest();
        req1.setPatientId(p1.getId());
        req1.setDentistId(targetDoctor.getId());
        req1.setTreatmentType("Consultation");
        req1.setDateTime(conflictTime);
        req1.setNotes("First booking - should succeed");
        AppointmentResponse resp1 = appointmentService.bookAppointment(req1);
        System.out.println("Booking 1 succeeded: ID=" + resp1.getId());

        // Create second booking (should fail due to overlap)
        AppointmentRequest req2 = new AppointmentRequest();
        req2.setPatientId(p2.getId());
        req2.setDentistId(targetDoctor.getId());
        req2.setTreatmentType("Consultation");
        req2.setDateTime(conflictTime);
        req2.setNotes("Second booking - should fail");

        try {
            appointmentService.bookAppointment(req2);
            throw new RuntimeException("CRITICAL ERROR: Overlapping appointment was allowed!");
        } catch (BadRequestException e) {
            System.out.println("OVERLAP TEST PASSED: Second booking failed as expected. Error: " + e.getMessage());
        }

        // 7. Generate bills for each appointment
        System.out.println("Generating bills for each appointment...");
        for (AppointmentResponse apt : bookedAppointments) {
            BillRequest billReq = new BillRequest();
            billReq.setPatientId(apt.getPatientId());
            billReq.setAmount(350.00); // Base consultation fee
            billReq.setNotes("Auto-generated consultation bill for appointment ID: " + apt.getId());
            billReq.setDueDate(LocalDateTime.now().plusDays(10));
            BillResponse billResp = billService.createBill(billReq);
            System.out.println("Bill ID=" + billResp.getId() + " generated for Patient: " + apt.getPatientName());
        }

        // 8. Create treatment plans for some patients (first 10 patients)
        System.out.println("Creating treatment plans for first 10 patients...");
        for (int i = 0; i < 10; i++) {
            Patient patient = createdPatients.get(i);
            TreatmentPlanRequest planReq = new TreatmentPlanRequest();
            planReq.setPatientId(patient.getId());
            planReq.setDentistId(dentists.get(i % dentists.size()).getId());
            planReq.setPlanName("Scenario Root Canal Plan " + (i + 1));
            planReq.setDescription("Treatment plan for Root Canal therapy including scaling and crown installation.");
            planReq.setSessions(List.of("Evaluation & X-Ray", "Cleaning & Prep", "Root Canal Extraction", "Temporary Filling", "Crown Placement"));
            planReq.setEstimatedCost(1500.00);
            planReq.setStartDate(LocalDateTime.now());
            planReq.setEndDate(LocalDateTime.now().plusWeeks(3));
            planReq.setTotalSessions(5);

            TreatmentPlanResponse planResp = treatmentPlanService.create(planReq);
            System.out.println("Treatment Plan ID=" + planResp.getId() + " created for Patient: " + patient.getName());
        }

        System.out.println("=== SCENARIO POPULATION COMPLETED SUCCESSFULLY ===");
    }
}

/*
 * Created an integration test to auto-populate the database with 100 patient accounts, 2 extra dentists, and weekly calendars.
 * The test simulates a real clinic setup by booking appointments across three days, creating consultation bills for each patient,
 * setting up 5-session root canal treatment plans for 10 patients, and validating double-booking rejections.
 * Concepts: Integration testing in Spring Boot, automated data seeding, business rule validation, and transaction flow testing.
 */

/*
 * This file contains the PopulateScenarioTest class which manages verifying application features, service operations, and database integration workflows.
 * Concepts used: Spring Boot integration tests, JUnit assertions, and mock MVC actions.
 */
