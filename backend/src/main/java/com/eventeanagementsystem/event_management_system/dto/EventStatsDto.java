package com.eventeanagementsystem.event_management_system.dto;

import com.eventeanagementsystem.event_management_system.db.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventStatsDto {
    private Event event;
    private Long attendeeCount;
}
