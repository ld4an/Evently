package com.eventeanagementsystem.event_management_system.notification;

import com.eventeanagementsystem.event_management_system.db.Attendee;
import com.eventeanagementsystem.event_management_system.db.Event;
import com.eventeanagementsystem.event_management_system.service.EmailService;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final EmailService emailService;
    private final AiEmailComposer aiEmailComposer;

    public NotificationService(EmailService emailService, AiEmailComposer aiEmailComposer) {
        this.emailService = emailService;
        this.aiEmailComposer = aiEmailComposer;
    }

    public void sendBookingApproved(Attendee attendee, Event event) {
        String to = attendee.getUser() != null
                ? attendee.getUser().getEmail()
                : attendee.getEmail(); // fallback if you store direct email on Attendee

        if (to == null || to.isBlank()) {
            System.err.println("No email available for attendee id=" + attendee.getId());
            return;
        }

        String subject = "Your booking has been approved - " + event.getName();
        String body = aiEmailComposer.composeApprovedEmailBody(attendee, event);

        emailService.sendEmail(to, subject, body);
    }

    public void sendBookingRejected(Attendee attendee, Event event) {
        String to = attendee.getUser() != null
                ? attendee.getUser().getEmail()
                : attendee.getEmail();

        if (to == null || to.isBlank()) {
            System.err.println("No email available for attendee id=" + attendee.getId());
            return;
        }

        String subject = "Your booking has been rejected - " + event.getName();
        String body = aiEmailComposer.composeRejectedEmailBody(attendee, event);

        emailService.sendEmail(to, subject, body);
    }

    public void sendAttendeeRemoved(Attendee attendee, Event event) {
        String to = attendee.getUser() != null
                ? attendee.getUser().getEmail()
                : attendee.getEmail();

        if (to == null || to.isBlank()) {
            System.err.println("No email available for attendee id=" + attendee.getId());
            return;
        }

        String subject = "Update: you were removed from " + event.getName();
        String body = aiEmailComposer.composeRemovedEmailBody(attendee, event);
        emailService.sendEmail(to, subject, body);
    }

    public void sendEventUpdated(Event event, String oldName, String oldDate, String oldLocation) {
        if (event.getAttendees() == null || event.getAttendees().isEmpty()) {
            return;
        }
        for (Attendee attendee : event.getAttendees()) {
            String to = attendee.getUser() != null
                    ? attendee.getUser().getEmail()
                    : attendee.getEmail();
            if (to == null || to.isBlank()) {
                continue;
            }
            String subject = "Event updated: " + event.getName();
            String body = aiEmailComposer.composeEventUpdatedEmailBody(attendee, event, oldName, oldDate, oldLocation);
            emailService.sendEmail(to, subject, body);
        }
    }
}

