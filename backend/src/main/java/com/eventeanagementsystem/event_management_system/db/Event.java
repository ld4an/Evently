package com.eventeanagementsystem.event_management_system.db;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    @CreationTimestamp
    private Date creationDate;
    private Date date;
    private String location;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties(value = {"event", "user"}, allowSetters = true)
    private List<Attendee> attendees = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    @JsonIgnoreProperties(value = {"events", "user"}, allowSetters = true)
    private Organizer organizer;

    private Integer maxAttendees;
}
