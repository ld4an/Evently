package com.eventeanagementsystem.event_management_system.service;

import com.eventeanagementsystem.event_management_system.db.Event;
import com.eventeanagementsystem.event_management_system.db.EventRepository;
import com.eventeanagementsystem.event_management_system.db.Organizer;
import com.eventeanagementsystem.event_management_system.db.OrganizerRepository;
import com.eventeanagementsystem.event_management_system.dto.EventStatsDto;
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

    public Event addEvent(Event event, int organizerId) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(()->new IllegalArgumentException("Organizer not found: " + organizerId));
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }
    public List<Event> listEvents() {
        return eventRepository.findAll();
    }
    public Event getEventById(int id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
    }

    public List<EventStatsDto> getEventsWithMostAttendees() {
        List<Object[]> results = eventRepository.findEventsWithMostAttendees();
        return results.stream()
                .map(row -> new EventStatsDto((Event) row[0], (Long) row[1]))
                .collect(Collectors.toList());
    }
}
