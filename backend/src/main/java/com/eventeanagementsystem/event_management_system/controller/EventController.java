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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Organizer> addOrganizer(@RequestBody @Valid Organizer organizer) {
        Organizer saved = organizerService.addOrganizer(organizer);
        return ResponseEntity.created(URI.create("/api/organizers/" + saved.getId())).body(saved);
    }

    // ---- EVENT ----
    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Event> addEvent(@RequestParam Integer organizerId,
                                          @RequestBody @Valid Event event) {
        Event saved = eventService.addEvent(event, organizerId);
        return ResponseEntity.created(URI.create("/api/events/" + saved.getId())).body(saved);
    }

    @PostMapping("/me/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Event> addEventForCurrentOrganizer(@RequestBody @Valid Event event) {
        Event saved = eventService.addEventForCurrentOrganizer(event);
        return ResponseEntity.created(URI.create("/api/events/" + saved.getId())).body(saved);
    }

    @PutMapping("/events/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public Event updateEvent(@PathVariable Integer id, @RequestBody @Valid Event event) {
        return eventService.updateEvent(id, event);
    }

    @DeleteMapping("/events/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // Publicly accessible, no annotation needed
    @GetMapping("/events")
    public List<Event> listEvents() {
        return eventService.listEvents();
    }

    // Publicly accessible, no annotation needed
    @GetMapping("/events/{id}")
    public Event getEvent(@PathVariable Integer id) {
        return eventService.getEventById(id);
    }

    @GetMapping("/events/most-attendees")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public List<EventStatsDto> getEventsWithMostAttendees() {
        return eventService.getEventsWithMostAttendees();
    }


    @GetMapping("/events/{eventId}/attendees")
    public List<Attendee> getAttendeesPerEvent(@PathVariable Integer eventId) {
        return attendeeService.getAttendeesPerEvent(eventId);
    }

    // === BOOKING ENDPOINTS ===

    // ATTENDEE: request to attend an event
    @PostMapping("/events/{eventId}/requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Attendee> requestToAttend(@PathVariable Integer eventId,
                                                    @RequestBody @Valid Attendee attendeeBody) {
        Attendee saved = attendeeService.requestToAttendEvent(eventId, attendeeBody);
        return ResponseEntity.created(URI.create("/api/attendees/" + saved.getId())).body(saved);
    }

    // ORGANIZER/ADMIN: see pending requests for an event
    @GetMapping("/events/{eventId}/requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public List<Attendee> getPendingRequests(@PathVariable Integer eventId) {
        return attendeeService.getPendingRequestsForEvent(eventId);
    }

    // ORGANIZER/ADMIN: approve request
    @PostMapping("/events/{eventId}/requests/{attendeeId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public Attendee approveRequest(@PathVariable Integer eventId,
                                   @PathVariable Integer attendeeId) {
        // eventId is mostly for URL clarity; we could also validate that attendee.event.id == eventId
        return attendeeService.approveAttendance(attendeeId);
    }

    // ORGANIZER/ADMIN: reject request
    @PostMapping("/events/{eventId}/requests/{attendeeId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public Attendee rejectRequest(@PathVariable Integer eventId,
                                  @PathVariable Integer attendeeId) {
        return attendeeService.rejectAttendance(attendeeId);
    }
    @GetMapping("/me/events")
    @PreAuthorize("hasRole('ORGANIZER')")
    public List<Event> getMyEvents(){
        return eventService.getMyEvents();
    }
}
