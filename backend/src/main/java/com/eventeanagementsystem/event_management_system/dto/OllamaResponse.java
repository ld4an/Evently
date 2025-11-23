package com.eventeanagementsystem.event_management_system.dto;

import lombok.Data;

@Data
public class OllamaResponse {
    private String model;
    private String created_at;
    private String response;
    private boolean done;
}

