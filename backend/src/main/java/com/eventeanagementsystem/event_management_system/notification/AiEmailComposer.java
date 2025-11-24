package com.eventeanagementsystem.event_management_system.notification;

import com.eventeanagementsystem.event_management_system.db.Attendee;
import com.eventeanagementsystem.event_management_system.db.Event;
import com.eventeanagementsystem.event_management_system.dto.OllamaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiEmailComposer {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String modelName;

    public AiEmailComposer(RestTemplate restTemplate, @Value("${ollama.base-url:http://localhost:11434}") String baseUrl, @Value("${ollama.model:llama3.1}") String modelName) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.modelName = modelName;
    }
    public String composeApprovedEmailBody(Attendee attendee, Event event) {
        String prompt = buildApprovedPrompt(attendee, event);
        return callOllamaOrFallback(prompt, defaultApprovedTemplate(attendee, event));
    }

    public String composeRejectedEmailBody(Attendee attendee, Event event) {
        String prompt = buildRejectedPrompt(attendee, event);
        return callOllamaOrFallback(prompt, defaultRejectedTemplate(attendee, event));
    }

    public String composeRemovedEmailBody(Attendee attendee, Event event) {
        String prompt = buildRemovedPrompt(attendee, event);
        return callOllamaOrFallback(prompt, defaultRemovedTemplate(attendee, event));
    }

    public String composeEventUpdatedEmailBody(Attendee attendee, Event event, String oldName, String oldDate, String oldLocation) {
        String prompt = buildEventUpdatedPrompt(attendee, event, oldName, oldDate, oldLocation);
        return callOllamaOrFallback(prompt, defaultEventUpdatedTemplate(attendee, event, oldName, oldDate, oldLocation));
    }
    private static final SimpleDateFormat EVENT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private String buildApprovedPrompt(Attendee attendee, Event event) {
        String eventDate = event.getDate() != null
                ? EVENT_DATE_FORMAT.format(event.getDate())
                : "the scheduled time";

        return """
                You are an assistant that writes friendly, concise emails.

                Task: Write an email in English to a user confirming that their booking request for an event has been APPROVED.

                Details:
                - Recipient name: %s
                - Event name: %s
                - Event date/time: %s
                - Keep it short (5–7 sentences max).
                - Use a friendly but professional tone.
                - Do NOT include any boilerplate about AI or that this was generated.
                """.formatted(
                safe(attendee.getName()),
                safe(event.getName()),
                eventDate
        );
    }

    private String buildRejectedPrompt(Attendee attendee, Event event) {
        String eventDate = event.getDate() != null
                ? EVENT_DATE_FORMAT.format(event.getDate())
                : "the scheduled time";

        return """
                You are an assistant that writes friendly, concise emails.

                Task: Write an email in English to a user informing them that their booking request for an event has been REJECTED.

                Details:
                - Recipient name: %s
                - Event name: %s
                - Event date/time: %s
                - Be polite, empathetic, and short (4–6 sentences).
                - Optionally suggest they look at other events.
                - Do NOT include any boilerplate about AI or that this was generated.
                """.formatted(
                safe(attendee.getName()),
                safe(event.getName()),
                eventDate
        );
    }

    private String buildRemovedPrompt(Attendee attendee, Event event) {
        String eventDate = event.getDate() != null
                ? EVENT_DATE_FORMAT.format(event.getDate())
                : "the scheduled time";

        return """
                Write a concise email to inform a user they have been removed from an event.
                Include event name and date/time. Keep it polite and 4-6 sentences.
                Recipient name: %s
                Event name: %s
                Event date/time: %s
                """.formatted(safe(attendee.getName()), safe(event.getName()), eventDate);
    }

    private String buildEventUpdatedPrompt(Attendee attendee, Event event, String oldName, String oldDate, String oldLocation) {
        String newDate = event.getDate() != null
                ? EVENT_DATE_FORMAT.format(event.getDate())
                : "the scheduled time";
        return """
                Write a concise email to notify a user that an event they joined was updated.
                Mention old vs new details if available. Keep it friendly and brief (5-7 sentences).
                Recipient name: %s
                Old name: %s
                New name: %s
                Old date: %s
                New date: %s
                Old location: %s
                New location: %s
                """.formatted(
                safe(attendee.getName()),
                safe(oldName),
                safe(event.getName()),
                safe(oldDate),
                newDate,
                safe(oldLocation),
                safe(event.getLocation())
        );
    }

    private String callOllamaOrFallback(String prompt, String fallback) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("model", modelName);
            request.put("prompt", prompt);
            request.put("stream", false);

            ResponseEntity<OllamaResponse> response = restTemplate.postForEntity(
                    baseUrl + "/api/generate",
                    request,
                    OllamaResponse.class
            );

            OllamaResponse body = response.getBody();
            if (body != null && body.getResponse() != null && !body.getResponse().isBlank()) {
                return body.getResponse().trim();
            }
        } catch (RestClientException ex) {
            // log if you want, but don't crash
            System.err.println("Ollama call failed, using fallback template: " + ex.getMessage());
        }
        return fallback;
    }

    private String defaultApprovedTemplate(Attendee attendee, Event event) {
        String name = safe(attendee.getName());
        String eventName = safe(event.getName());
        String eventDate = event.getDate() != null
                ? EVENT_DATE_FORMAT.format(event.getDate())
                : "the scheduled time";
        String organizer = organizerName(event);

        return """
                Hi %s,

                Your booking request for the event "%s" has been approved.
                The event is scheduled for %s.

                We’re looking forward to seeing you there!

                Best regards,
                %s
                """.formatted(name, eventName, eventDate, organizer);
    }

    private String defaultRejectedTemplate(Attendee attendee, Event event) {
        String name = safe(attendee.getName());
        String eventName = safe(event.getName());
        String organizer = organizerName(event);

        return """
                Hi %s,

                Unfortunately, your booking request for the event "%s" could not be approved.
                We’re sorry for the inconvenience and hope you’ll consider joining our other events.

                Best regards,
                %s
                """.formatted(name, eventName, organizer);
    }

    private String defaultRemovedTemplate(Attendee attendee, Event event) {
        String name = safe(attendee.getName());
        String eventName = safe(event.getName());
        String eventDate = event.getDate() != null
                ? EVENT_DATE_FORMAT.format(event.getDate())
                : "the scheduled time";
        String organizer = organizerName(event);

        return """
                Hi %s,

                You have been removed from the event "%s" scheduled for %s.
                If you believe this is a mistake, please contact the organizer.

                Best regards,
                %s
                """.formatted(name, eventName, eventDate, organizer);
    }

    private String defaultEventUpdatedTemplate(Attendee attendee, Event event, String oldName, String oldDate, String oldLocation) {
        String name = safe(attendee.getName());
        String newDate = event.getDate() != null
                ? EVENT_DATE_FORMAT.format(event.getDate())
                : "the scheduled time";
        String organizer = organizerName(event);

        return """
                Hi %s,

                The event you joined has been updated.
                Old name/date/location: %s / %s / %s
                New name/date/location: %s / %s / %s

                Please review the new details. We hope to see you there!

                Best regards,
                %s
                """.formatted(
                name,
                safe(oldName), safe(oldDate), safe(oldLocation),
                safe(event.getName()), newDate, safe(event.getLocation()),
                organizer
        );
    }

    private String organizerName(Event event) {
        if (event != null && event.getOrganizer() != null && event.getOrganizer().getName() != null) {
            return event.getOrganizer().getName();
        }
        return "Event Organizer";
    }

    private String safe(String value) {
        return value != null ? value : "there";
    }
}

