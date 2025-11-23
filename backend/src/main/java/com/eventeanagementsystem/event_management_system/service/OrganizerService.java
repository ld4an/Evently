package com.eventeanagementsystem.event_management_system.service;

import com.eventeanagementsystem.event_management_system.db.AppUser;
import com.eventeanagementsystem.event_management_system.db.AppUserRepository;
import com.eventeanagementsystem.event_management_system.db.Organizer;
import com.eventeanagementsystem.event_management_system.db.OrganizerRepository;
import com.eventeanagementsystem.event_management_system.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizerService {

    private final OrganizerRepository organizerRepository;
    private final AppUserRepository appUserRepository;

    public OrganizerService(OrganizerRepository organizerRepository,
                            AppUserRepository appUserRepository) {
        this.organizerRepository = organizerRepository;
        this.appUserRepository = appUserRepository;
    }

    public Organizer addOrganizer(Organizer organizer) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("No authenticated user in context");
        }

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        organizer.setUser(user);
        return organizerRepository.save(organizer);
    }

    public Organizer getMyOrganizerProfile() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("No authenticated user in context");
        }

        return organizerRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalStateException("Organizer profile not found for user " + email));
    }

    public List<Organizer> listOrganizers() {
        return organizerRepository.findAll();
    }

}
