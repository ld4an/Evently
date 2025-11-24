package com.eventeanagementsystem.event_management_system.controller;

import com.eventeanagementsystem.event_management_system.db.Attendee;
import com.eventeanagementsystem.event_management_system.db.Event;
import com.eventeanagementsystem.event_management_system.service.AttendeeService;
import com.eventeanagementsystem.event_management_system.service.AuthService;
import com.eventeanagementsystem.event_management_system.dto.UpdateCredentialsRequest;
import com.eventeanagementsystem.event_management_system.dto.AuthResponse;
import com.eventeanagementsystem.event_management_system.security.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/me")
public class MeController {
    private final AttendeeService attendeeService;
    private final AuthService authService;
    
    public MeController(AttendeeService attendeeService, AuthService authService) {
        this.attendeeService = attendeeService;
        this.authService = authService;
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ATTENDEE')")
    public List<Attendee> getMyRequests() {
        return attendeeService.getMyRequests();
    }

    @GetMapping("/attending-events")
    @PreAuthorize("isAuthenticated()")
    public List<Event> getMyEvents() {
        // Get all attendee requests for the current user
        List<Attendee> myRequests = attendeeService.getMyRequests();
        // Extract the events from approved attendees
        return myRequests.stream()
                .filter(attendee -> attendee.getEvent() != null)
                .map(Attendee::getEvent)
                .distinct()
                .collect(Collectors.toList());
    }

    @PutMapping("/credentials")
    @PreAuthorize("isAuthenticated()")
    public AuthResponse updateCredentials(@RequestBody UpdateCredentialsRequest request) {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        if (currentEmail == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return authService.updateCredentials(currentEmail, request);
    }
}
