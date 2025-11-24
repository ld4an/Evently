package com.eventeanagementsystem.event_management_system.db;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Organizer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Organizer name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid email address")
    private String email;

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"organizer", "attendees"}, allowSetters = true)
    private List<Event> events;

    @OneToOne
    @JoinColumn(name = "user_id") // FK to app_user table
    private AppUser user;
}
