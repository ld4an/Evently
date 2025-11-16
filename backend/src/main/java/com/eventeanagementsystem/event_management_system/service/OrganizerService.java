package com.eventeanagementsystem.event_management_system.service;

import com.eventeanagementsystem.event_management_system.db.Organizer;
import com.eventeanagementsystem.event_management_system.db.OrganizerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizerService {

    private final OrganizerRepository organizerRepository;

    @Autowired
    public OrganizerService(OrganizerRepository organizerRepository) {
        this.organizerRepository = organizerRepository;
    }

    public Organizer addOrganizer(Organizer organizer) {
        return organizerRepository.save(organizer);
    }
}
