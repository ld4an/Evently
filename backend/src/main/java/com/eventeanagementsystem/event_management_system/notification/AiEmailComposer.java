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

    // SimpleDateFormat is NOT thread-safe; in a Spring singleton service this can cause bugs.
    // Use per-call instance via formatEventDate().
    private static final String EVENT_DATE_PATTERN = "yyyy-MM-dd HH:mm";

    public AiEmailComposer(
            RestTemplate restTemplate,
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:llama3.1}") String modelName
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.modelName = modelName;
    }

    public String composeApprovedEmailBody(Attendee attendee, Event event) {
        String prompt = buildApprovedPrompt(attendee, event);
        String fallback = defaultApprovedTemplate(attendee, event);
        String text = callOllamaOrFallback(prompt, fallback);
        return enforceSignature(text, event);
    }

    public String composeRejectedEmailBody(Attendee attendee, Event event) {
        String prompt = buildRejectedPrompt(attendee, event);
        String fallback = defaultRejectedTemplate(attendee, event);
        String text = callOllamaOrFallback(prompt, fallback);
        return enforceSignature(text, event);
    }

    public String composeRemovedEmailBody(Attendee attendee, Event event) {
        String prompt = buildRemovedPrompt(attendee, event);
        String fallback = defaultRemovedTemplate(attendee, event);
        String text = callOllamaOrFallback(prompt, fallback);
        return enforceSignature(text, event);
    }

    public String composeEventUpdatedEmailBody(Attendee attendee, Event event, String oldName, String oldDate, String oldLocation) {
        String prompt = buildEventUpdatedPrompt(attendee, event, oldName, oldDate, oldLocation);
        String fallback = defaultEventUpdatedTemplate(attendee, event, oldName, oldDate, oldLocation);
        String text = callOllamaOrFallback(prompt, fallback);
        return enforceSignature(text, event);
    }

    // ------------------------
    // Prompt building
    // ------------------------

    private String buildApprovedPrompt(Attendee attendee, Event event) {
        String recipientName = safePersonName(attendee != null ? attendee.getName() : null);
        String eventName = safeEventName(event != null ? event.getName() : null);
        String eventDate = formatEventDate(event);

        return """
                You are an assistant that writes friendly, concise emails in English.

                Task: Write an email to a user confirming that their booking request for an event has been APPROVED.

                Details:
                - Recipient name: %s
                - Event name: %s
                - Event date/time: %s

                Rules:
                - Keep it short (5–7 sentences max).
                - Friendly but professional tone.
                - Output ONLY the email body (no extra commentary).
                - Do NOT use placeholders like [Event Name], [Your Name], [Your Team Name], etc.
                - End the email with this exact sign-off (exact wording and line breaks):
                  Best regards,
                  %s Team
                """.formatted(recipientName, eventName, eventDate, eventName);
    }

    private String buildRejectedPrompt(Attendee attendee, Event event) {
        String recipientName = safePersonName(attendee != null ? attendee.getName() : null);
        String eventName = safeEventName(event != null ? event.getName() : null);
        String eventDate = formatEventDate(event);

        return """
                You are an assistant that writes friendly, concise emails in English.

                Task: Write an email to a user informing them that their booking request for an event has been REJECTED.

                Details:
                - Recipient name: %s
                - Event name: %s
                - Event date/time: %s

                Rules:
                - Be polite and empathetic.
                - Keep it short (4–6 sentences).
                - Output ONLY the email body (no extra commentary).
                - Do NOT use placeholders like [Event Name], [Your Name], [Your Team Name], etc.
                - End the email with this exact sign-off (exact wording and line breaks):
                  Best regards,
                  %s Team
                """.formatted(recipientName, eventName, eventDate, eventName);
    }

    private String buildRemovedPrompt(Attendee attendee, Event event) {
        String recipientName = safePersonName(attendee != null ? attendee.getName() : null);
        String eventName = safeEventName(event != null ? event.getName() : null);
        String eventDate = formatEventDate(event);

        return """
                You are an assistant that writes friendly, concise emails in English.

                Task: Write an email to inform a user they have been removed from an event.

                Details:
                - Recipient name: %s
                - Event name: %s
                - Event date/time: %s

                Rules:
                - Keep it polite and short (4–6 sentences).
                - Output ONLY the email body (no extra commentary).
                - Do NOT use placeholders like [Event Name], [Your Name], [Your Team Name], etc.
                - End the email with this exact sign-off (exact wording and line breaks):
                  Best regards,
                  %s Team
                """.formatted(recipientName, eventName, eventDate, eventName);
    }

    private String buildEventUpdatedPrompt(Attendee attendee, Event event, String oldName, String oldDate, String oldLocation) {
        String recipientName = safePersonName(attendee != null ? attendee.getName() : null);
        String newName = safeEventName(event != null ? event.getName() : null);
        String newDate = formatEventDate(event);
        String newLocation = safeField(event != null ? event.getLocation() : null, "the updated location");

        String safeOldName = safeField(oldName, "the previous name");
        String safeOldDate = safeField(oldDate, "the previous date/time");
        String safeOldLocation = safeField(oldLocation, "the previous location");

        return """
                You are an assistant that writes friendly, concise emails in English.

                Task: Write an email to notify a user that an event they joined was updated.

                Details:
                - Recipient name: %s
                - Old name: %s
                - New name: %s
                - Old date/time: %s
                - New date/time: %s
                - Old location: %s
                - New location: %s

                Rules:
                - Keep it friendly and brief (5–7 sentences).
                - Output ONLY the email body (no extra commentary).
                - Do NOT use placeholders like [Event Name], [Your Name], [Your Team Name], etc.
                - End the email with this exact sign-off (exact wording and line breaks):
                  Best regards,
                  %s Team
                """.formatted(
                recipientName,
                safeOldName,
                newName,
                safeOldDate,
                newDate,
                safeOldLocation,
                newLocation,
                newName
        );
    }

    // ------------------------
    // Ollama call
    // ------------------------

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
            if (body != null && body.getResponse() != null) {
                String txt = body.getResponse().trim();
                if (!txt.isBlank()) {
                    return txt;
                }
            }
        } catch (RestClientException ex) {
            System.err.println("Ollama call failed, using fallback template: " + ex.getMessage());
        } catch (Exception ex) {
            // safety net: don't crash notification flow
            System.err.println("Unexpected error composing email, using fallback template: " + ex.getMessage());
        }
        return fallback;
    }

    // ------------------------
    // Templates (fallback)
    // ------------------------

    private String defaultApprovedTemplate(Attendee attendee, Event event) {
        String name = safePersonName(attendee != null ? attendee.getName() : null);
        String eventName = safeEventName(event != null ? event.getName() : null);
        String eventDate = formatEventDate(event);

        return """
                Hi %s,

                Your booking request for the event "%s" has been approved.
                The event is scheduled for %s.

                We’re looking forward to seeing you there!

                Best regards,
                %s Team
                """.formatted(name, eventName, eventDate, eventName);
    }

    private String defaultRejectedTemplate(Attendee attendee, Event event) {
        String name = safePersonName(attendee != null ? attendee.getName() : null);
        String eventName = safeEventName(event != null ? event.getName() : null);

        return """
                Hi %s,

                Unfortunately, your booking request for the event "%s" could not be approved.
                We’re sorry for the inconvenience and hope you’ll consider joining our other events.

                Best regards,
                %s Team
                """.formatted(name, eventName, eventName);
    }

    private String defaultRemovedTemplate(Attendee attendee, Event event) {
        String name = safePersonName(attendee != null ? attendee.getName() : null);
        String eventName = safeEventName(event != null ? event.getName() : null);
        String eventDate = formatEventDate(event);

        return """
                Hi %s,

                You have been removed from the event "%s" scheduled for %s.
                If you believe this is a mistake, please contact the organizer.

                Best regards,
                %s Team
                """.formatted(name, eventName, eventDate, eventName);
    }

    private String defaultEventUpdatedTemplate(Attendee attendee, Event event, String oldName, String oldDate, String oldLocation) {
        String name = safePersonName(attendee != null ? attendee.getName() : null);
        String newName = safeEventName(event != null ? event.getName() : null);
        String newDate = formatEventDate(event);
        String newLocation = safeField(event != null ? event.getLocation() : null, "the updated location");

        String safeOldName = safeField(oldName, "the previous name");
        String safeOldDate = safeField(oldDate, "the previous date/time");
        String safeOldLocation = safeField(oldLocation, "the previous location");

        return """
                Hi %s,

                The event you joined has been updated.
                Old name/date/location: %s / %s / %s
                New name/date/location: %s / %s / %s

                Please review the new details. We hope to see you there!

                Best regards,
                %s Team
                """.formatted(
                name,
                safeOldName, safeOldDate, safeOldLocation,
                newName, newDate, newLocation,
                newName
        );
    }

    // ------------------------
    // Signature enforcement (prevents [Event Name] Team forever)
    // ------------------------

    private String enforceSignature(String text, Event event) {
        String eventName = safeEventName(event != null ? event.getName() : null);
        String signature = "\n\nBest regards,\n" + eventName + " Team";

        if (text == null || text.isBlank()) {
            return signature.trim();
        }

        String normalized = text.replace("\r\n", "\n").trim();
        String lower = normalized.toLowerCase();

        int idx = lower.lastIndexOf("best regards");
        if (idx >= 0) {
            // Cut from "Best regards" to end and replace with our enforced signature.
            String before = normalized.substring(0, idx).trim();
            return before + signature;
        }

        // If no sign-off, append it.
        return normalized + signature;
    }

    // ------------------------
    // Helpers
    // ------------------------

    private String formatEventDate(Event event) {
        if (event == null || event.getDate() == null) {
            return "the scheduled time";
        }
        // Per-call SimpleDateFormat to avoid thread-safety issues.
        return new SimpleDateFormat(EVENT_DATE_PATTERN).format(event.getDate());
    }

    private String safePersonName(String value) {
        return safeField(value, "there");
    }

    private String safeEventName(String value) {
        return safeField(value, "Event");
    }

    private String safeField(String value, String fallback) {
        if (value == null) return fallback;
        String v = value.trim();
        return v.isEmpty() ? fallback : v;
    }
}

