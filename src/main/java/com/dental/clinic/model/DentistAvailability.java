package com.dental.clinic.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "dentist_availability", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dentist_id", "day_of_week"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DentistAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", nullable = false)
    private User dentist;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}

/*
 * This file contains the DentistAvailability class which manages JPA persistence entities mapping database tables to Java object structures.
 * Concepts used: JPA/ORM annotations, database schema definitions, and Lombok builders.
 */
