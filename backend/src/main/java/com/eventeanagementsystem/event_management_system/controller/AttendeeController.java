package com.eventeanagementsystem.event_management_system.controller;

import com.eventeanagementsystem.event_management_system.db.Attendee;
import com.eventeanagementsystem.event_management_system.db.AttendeeRepository;
import com.eventeanagementsystem.event_management_system.service.AttendeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendees")
public class AttendeeController {
    private final AttendeeService attendeeService;

    @Autowired
    public AttendeeController(AttendeeService attendeeService) {
        this.attendeeService = attendeeService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Attendee> addAttendee(@RequestBody Attendee attendee){
        Attendee saved = attendeeService.addAttendee(attendee);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Attendee> getAttendeeById(@PathVariable int id){
        Attendee attendee = attendeeService.getAttendeeById(id);
        return ResponseEntity.ok(attendee);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Iterable<Attendee>> listAttendees(){
        List<Attendee> attendees = attendeeService.listAttendees();
        return ResponseEntity.ok(attendees);
    }
    // 4. Asignare attendee la event
    // POST /api/attendees/{attendeeId}/events/{eventId}
    @PostMapping("/{attendeeId}/events/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Attendee> assignAttendeeToEvent(@PathVariable int attendeeId,
                                                          @PathVariable int eventId) {
        Attendee updated = attendeeService.assignAttendeeToEvent(attendeeId, eventId);
        return ResponseEntity.ok(updated);
    }

    // 5. Dez-asignare attendee de la event
    @PutMapping("/{attendeeId}/remove-event")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Attendee> removeAttendeeFromEvent(@PathVariable int attendeeId) {
        Attendee updated = attendeeService.removeAttendeeFromEvent(attendeeId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{attendeeId}/ban")
    public ResponseEntity<Attendee> banAttendee(@PathVariable int attendeeId) {
        Attendee updated = attendeeService.banAttendeeFromEvent(attendeeId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{attendeeId}/unban")
    public ResponseEntity<Attendee> unbanAttendee(@PathVariable int attendeeId) {
        Attendee updated = attendeeService.unbanAttendee(attendeeId);
        return ResponseEntity.ok(updated);
    }

}
