package com.eventeanagementsystem.event_management_system.controller;

import com.eventeanagementsystem.event_management_system.db.Attendee;
import com.eventeanagementsystem.event_management_system.db.Event;
import com.eventeanagementsystem.event_management_system.db.Organizer;
import com.eventeanagementsystem.event_management_system.dto.EventStatsDto;
import com.eventeanagementsystem.event_management_system.service.AttendeeService;
import com.eventeanagementsystem.event_management_system.service.EventService;
import com.eventeanagementsystem.event_management_system.service.OrganizerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;
    private final AttendeeService attendeeService;
    private final OrganizerService organizerService;

    public EventController(EventService eventService,
                           AttendeeService attendeeService,
                           OrganizerService organizerService) {
        this.eventService = eventService;
        this.attendeeService = attendeeService;
        this.organizerService = organizerService;
    }

    // ---- ORGANIZER ----
    @PostMapping("/organizers")
    public ResponseEntity<Organizer> addOrganizer(@RequestBody @Valid Organizer organizer) {
        Organizer saved = organizerService.addOrganizer(organizer);
        return ResponseEntity.created(URI.create("/api/organizers/" + saved.getId())).body(saved);
    }

    // ---- EVENT ----
    @PostMapping("/events")
    public ResponseEntity<Event> addEvent(@RequestParam int organizerId,
                                          @RequestBody @Valid Event event) {
        Event saved = eventService.addEvent(event, organizerId);
        return ResponseEntity.created(URI.create("/api/events/" + saved.getId())).body(saved);
    }

    @GetMapping("/events")
    public List<Event> listEvents() {
        return eventService.listEvents();
    }

    @GetMapping("/events/{id}")
    public Event getEvent(@PathVariable int id) {
        return eventService.getEventById(id);
    }

    @GetMapping("/events/most-attendees")
    public List<EventStatsDto> getEventsWithMostAttendees() {
        return eventService.getEventsWithMostAttendees();
    }

    // ---- ATTENDEE ----
    @PostMapping("/attendees")
    public ResponseEntity<Attendee> addAttendee(@RequestBody @Valid Attendee attendee) {
        Attendee saved = attendeeService.addAttendee(attendee);
        return ResponseEntity.created(URI.create("/api/attendees/" + saved.getId())).body(saved);
    }

    @GetMapping("/attendees")
    public List<Attendee> listAttendees() {
        return attendeeService.listAttendees();
    }

    @PostMapping("/events/{eventId}/attendees/{attendeeId}")
    public Attendee assignAttendeeToEvent(@PathVariable int eventId,
                                          @PathVariable int attendeeId) {
        return attendeeService.assignAttendeeToEvent(attendeeId, eventId);
    }

    @GetMapping("/events/{eventId}/attendees")
    public List<Attendee> getAttendeesPerEvent(@PathVariable int eventId) {
        return attendeeService.getAttendeesPerEvent(eventId);
    }
}
