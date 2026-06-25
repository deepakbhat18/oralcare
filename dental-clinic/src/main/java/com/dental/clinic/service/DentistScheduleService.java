package com.dental.clinic.service;

import com.dental.clinic.dto.request.DentistAvailabilityRequest;
import com.dental.clinic.dto.request.DentistLeaveRequest;
import com.dental.clinic.dto.response.DentistAvailabilityResponse;
import com.dental.clinic.dto.response.DentistLeaveResponse;
import com.dental.clinic.exception.BadRequestException;
import com.dental.clinic.exception.ResourceNotFoundException;
import com.dental.clinic.model.DentistAvailability;
import com.dental.clinic.model.DentistLeave;
import com.dental.clinic.model.DentistLeave.LeaveStatus;
import com.dental.clinic.model.User;
import com.dental.clinic.repository.DentistAvailabilityRepository;
import com.dental.clinic.repository.DentistLeaveRepository;
import com.dental.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class DentistScheduleService {

    private final DentistAvailabilityRepository availabilityRepository;
    private final DentistLeaveRepository leaveRepository;
    private final UserRepository userRepository;



    public List<DentistAvailabilityResponse> getAvailabilities(Long dentistId) {
        return availabilityRepository.findByDentistId(dentistId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DentistAvailabilityResponse saveAvailability(DentistAvailabilityRequest request) {
        User dentist = userRepository.findById(request.getDentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist", request.getDentistId()));

        if (dentist.getRole() != User.Role.DENTIST) {
            throw new BadRequestException("User must be a dentist");
        }

        DentistAvailability availability = availabilityRepository
                .findByDentistIdAndDayOfWeek(request.getDentistId(), request.getDayOfWeek())
                .orElseGet(() -> DentistAvailability.builder()
                        .dentist(dentist)
                        .dayOfWeek(request.getDayOfWeek())
                        .build());

        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());

        return mapToResponse(availabilityRepository.save(availability));
    }

    @Transactional
    public void deleteAvailability(Long id) {
        if (!availabilityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Availability", id);
        }
        availabilityRepository.deleteById(id);
    }



    public List<DentistLeaveResponse> getLeaves(Long dentistId) {
        return leaveRepository.findByDentistId(dentistId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DentistLeaveResponse requestLeave(DentistLeaveRequest request) {
        User dentist = userRepository.findById(request.getDentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist", request.getDentistId()));

        if (dentist.getRole() != User.Role.DENTIST) {
            throw new BadRequestException("User must be a dentist");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        DentistLeave leave = DentistLeave.builder()
                .dentist(dentist)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        return mapToResponse(leaveRepository.save(leave));
    }

    @Transactional
    public DentistLeaveResponse approveLeave(Long id) {
        DentistLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));
        leave.setStatus(LeaveStatus.APPROVED);
        return mapToResponse(leaveRepository.save(leave));
    }

    @Transactional
    public DentistLeaveResponse rejectLeave(Long id) {
        DentistLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));
        leave.setStatus(LeaveStatus.REJECTED);
        return mapToResponse(leaveRepository.save(leave));
    }



    public void validateDentistSchedule(Long dentistId, LocalDateTime dateTime, int durationMinutes) {
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();
        LocalTime endTime = time.plusMinutes(durationMinutes);


        List<DentistLeave> overlappingLeaves = leaveRepository.findApprovedLeaveOnDate(dentistId, date);
        if (!overlappingLeaves.isEmpty()) {
            throw new BadRequestException("The dentist is on approved leave on " + date);
        }



        int dayOfWeek = date.getDayOfWeek().getValue();
        DentistAvailability availability = availabilityRepository
                .findByDentistIdAndDayOfWeek(dentistId, dayOfWeek)
                .orElseThrow(() -> new BadRequestException("The dentist does not work on " + date.getDayOfWeek()));


        if (time.isBefore(availability.getStartTime()) || endTime.isAfter(availability.getEndTime())) {
            throw new BadRequestException(String.format(
                    "The slot is outside working hours. Dentist available: %s - %s",
                    availability.getStartTime(), availability.getEndTime()));
        }
    }

    private DentistAvailabilityResponse mapToResponse(DentistAvailability a) {
        return DentistAvailabilityResponse.builder()
                .id(a.getId())
                .dentistId(a.getDentist().getId())
                .dentistName(a.getDentist().getName())
                .dayOfWeek(a.getDayOfWeek())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .build();
    }

    private DentistLeaveResponse mapToResponse(DentistLeave l) {
        return DentistLeaveResponse.builder()
                .id(l.getId())
                .dentistId(l.getDentist().getId())
                .dentistName(l.getDentist().getName())
                .startDate(l.getStartDate())
                .endDate(l.getEndDate())
                .reason(l.getReason())
                .status(l.getStatus())
                .build();
    }
}

/*
 * This file contains the DentistScheduleService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
