package com.eventeanagementsystem.event_management_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email(message = "Please provide a valid email address")
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String role;

}
