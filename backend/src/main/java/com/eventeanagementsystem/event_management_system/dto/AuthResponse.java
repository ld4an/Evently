package com.eventeanagementsystem.event_management_system.dto;

import com.eventeanagementsystem.event_management_system.db.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserRole role;

}
