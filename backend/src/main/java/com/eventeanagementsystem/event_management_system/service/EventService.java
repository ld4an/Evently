package com.eventeanagementsystem.event_management_system.service;

import com.eventeanagementsystem.event_management_system.db.*;
import com.eventeanagementsystem.event_management_system.dto.EventStatsDto;
import com.eventeanagementsystem.event_management_system.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;

    @Autowired
    public EventService(EventRepository eventRepository,OrganizerRepository organizerRepository) {
        this.organizerRepository = organizerRepository;
        this.eventRepository = eventRepository;
    }

    public Event addEvent(Event event, Integer organizerId) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(()->new IllegalArgumentException("Organizer not found: " + organizerId));
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }
    public List<Event> listEvents() {
        return eventRepository.findAll();
    }
    public Event getEventById(Integer id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
    }

    public List<EventStatsDto> getEventsWithMostAttendees() {
        List<Object[]> results = eventRepository.findEventsWithMostAttendees();
        return results.stream()
                .map(row -> new EventStatsDto((Event) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }

    private void ensureCanManageEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event is null");
        }

        // Admin can manage anything
        if (SecurityUtils.hasRole("ADMIN")) {
            return;
        }

        // Organizer must own the event
        if (SecurityUtils.hasRole("ORGANIZER")) {
            String email = SecurityUtils.getCurrentUserEmail();
            String ownerEmail = event.getOrganizer() != null && event.getOrganizer().getUser() != null
                    ? event.getOrganizer().getUser().getEmail()
                    : null;

            if (email != null && email.equals(ownerEmail)) {
                return;
            }
        }

        throw new SecurityException("You are not allowed to manage this event");
    }
    

    public List<Event> getMyEvents() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("No authenticated user");
        }

        Organizer organizer = organizerRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalStateException("Organizer profile not found for user " + email));

        return eventRepository.findByOrganizerId(organizer.getId());
    }

    // Example: guard a management operation
    public List<Attendee> getAttendeesPerEvent(Integer eventId, AttendeeRepository attendeeRepository) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        ensureCanManageEvent(event);  // <- ownership check

        return attendeeRepository.findByEventId(eventId);
    }
}
