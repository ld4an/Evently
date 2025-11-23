package com.eventeanagementsystem.event_management_system.controller;

import com.eventeanagementsystem.event_management_system.db.Attendee;
import com.eventeanagementsystem.event_management_system.service.AttendeeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public class MeController {
    private final AttendeeService attendeeService;
    public MeController(AttendeeService attendeeService) {
        this.attendeeService = attendeeService;
    }


    @GetMapping("/requests")
    @PreAuthorize("hasRole('ATTENDEE')")
    public List<Attendee> getMyRequests() {
        return attendeeService.getMyRequests();
    }
}
