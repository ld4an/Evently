package com.eventeanagementsystem.event_management_system.service;

import com.eventeanagementsystem.event_management_system.db.Attendee;
import com.eventeanagementsystem.event_management_system.db.AttendeeRepository;
import com.eventeanagementsystem.event_management_system.db.Event;
import com.eventeanagementsystem.event_management_system.db.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final EventRepository eventRepository;

    @Autowired
    public AttendeeService(AttendeeRepository attendeeRepository, EventRepository eventRepository) {
        this.attendeeRepository = attendeeRepository;
        this.eventRepository = eventRepository;
    }

    public Attendee addAttendee(Attendee attendee) {
        return attendeeRepository.save(attendee);
    }


    public Attendee assignAttendeeToEvent(int attendeeId, int eventId) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new IllegalArgumentException("Attendee not found: " + attendeeId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        attendee.setEvent(event);
        return attendeeRepository.save(attendee);
    }

    public List<Attendee> listAttendees() {
        return attendeeRepository.findAll();
    }

    public List<Attendee> getAttendeesPerEvent(int eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        return attendeeRepository.findByEvent(event);
    }
}
