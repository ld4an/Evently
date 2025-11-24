package com.eventeanagementsystem.event_management_system.db;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attendee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    @NotBlank
    private String email;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties(value = {"attendees", "organizer"}, allowSetters = true)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;
}
