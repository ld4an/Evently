package com.eventeanagementsystem.event_management_system.service;

import com.eventeanagementsystem.event_management_system.db.*;
import com.eventeanagementsystem.event_management_system.notification.NotificationService;
import com.eventeanagementsystem.event_management_system.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final EventRepository eventRepository;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;

    @Autowired
    public AttendeeService(AttendeeRepository attendeeRepository, EventRepository eventRepository, AppUserRepository appUserRepository, NotificationService notificationService) {
        this.attendeeRepository = attendeeRepository;
        this.eventRepository = eventRepository;
        this.appUserRepository = appUserRepository;
        this.notificationService = notificationService;
    }

    public Attendee addAttendee(Attendee attendee) {
        return attendeeRepository.save(attendee);
    }


    public Attendee getAttendeeById(int attendeeId) {
        return attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new IllegalArgumentException("Attendee not found: " + attendeeId));
    }

    /**
     * Asignează un participant unui eveniment.
     * Presupune:
     * - Attendee are un camp `event`
     * - Event are un `List<Attendee> attendees`
     * - Event poate avea un maxAttendees (opțional)
     */
    public Attendee assignAttendeeToEvent(int attendeeId, int eventId) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new IllegalArgumentException("Attendee not found: " + attendeeId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        // already assigned to this event?
        if (attendee.getEvent() != null && attendee.getEvent().getId() == eventId) {
            // either do nothing or throw an exception
            return attendee; // aici aleg să nu fac nimic
        }

        // (optional) block moving attendee to a different event
        if (attendee.getEvent() != null) {
            throw new IllegalStateException("Attendee is already assigned to another event");
        }

        // (opțional) verificare de capacitate
        if (event.getMaxAttendees() != null &&
                event.getAttendees() != null &&
                event.getAttendees().size() >= event.getMaxAttendees()) {
            throw new IllegalStateException("Event is full, cannot add more attendees");
        }

        // menținem relația bidirecțională
        attendee.setEvent(event);               // partea ManyToOne
        event.getAttendees().add(attendee);     // partea OneToMany

        return attendeeRepository.save(attendee);

    }

    /**
     * Dez-asignează un participant de la eveniment (dacă ai nevoie de așa ceva).
     */
    public Attendee removeAttendeeFromEvent(int attendeeId) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new IllegalArgumentException("Attendee not found: " + attendeeId));

        Event event = attendee.getEvent();
        if (event != null && event.getAttendees() != null) {
            event.getAttendees().remove(attendee);
        }
        attendee.setEvent(null);

        attendeeRepository.save(attendee);
        return attendee;
    }

    /**
     * Returnează toți participanții unui eveniment.
     */

    public List<Attendee> listAttendees() {
        return attendeeRepository.findAll();
    }

    public List<Attendee> getAttendeesPerEvent(int eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        return attendeeRepository.findByEvent(event);
    }

    // === BOOKING LOGIC ===

    /** Attendee sends a request to attend an event */
    public Attendee requestToAttendEvent(int eventId, Attendee attendeeData, AppUser user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        Attendee attendee = new Attendee();
        // copy whatever fields you have (name, email, etc.)
        attendee.setEvent(event);
        attendee.setStatus(AttendanceStatus.PENDING);

        return attendeeRepository.save(attendee);
    }

    /** Organizer/Admin sees pending requests for event */
    public List<Attendee> getPendingRequestsForEvent(int eventId) {
        return attendeeRepository.findByEventIdAndStatus(eventId, AttendanceStatus.PENDING);
    }

    /** Approve a pending request */
    public Attendee approveAttendance(int attendeeId) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new IllegalArgumentException("Attendee not found: " + attendeeId));

        attendee.setStatus(AttendanceStatus.APPROVED);
        Attendee saved = attendeeRepository.save(attendee);

        Event event = attendee.getEvent();
        if (event != null && event.getAttendees() != null) {
            notificationService.sendBookingApproved(saved, event);
        }

        return saved;
    }

    /** Reject a pending request */
    public Attendee rejectAttendance(int attendeeId) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new IllegalArgumentException("Attendee not found: " + attendeeId));

        attendee.setStatus(AttendanceStatus.REJECTED);
        Attendee saved = attendeeRepository.save(attendee);
        Event event = attendee.getEvent();
        if (event != null && event.getAttendees() != null) {
            notificationService.sendBookingRejected(saved, event);
        }

        return saved;
    }

    // example: request to attend event (from previous step)
    public Attendee requestToAttendEvent(int eventId, Attendee attendeeData) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("No authenticated user in context");
        }

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        Attendee attendee = new Attendee();
        // copy fields the user is allowed to set (e.g. name, phone)
        attendee.setEvent(event);
        attendee.setUser(user);
        attendee.setStatus(AttendanceStatus.PENDING);

        return attendeeRepository.save(attendee);
    }

    public List<Attendee> getMyRequests() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("No authenticated user in context");
        }
        return attendeeRepository.findByUserEmail(email);
    }
}
