package com.eventeanagementsystem.event_management_system.service;

import com.eventeanagementsystem.event_management_system.db.AppUser;
import com.eventeanagementsystem.event_management_system.db.AppUserRepository;
import com.eventeanagementsystem.event_management_system.db.Organizer;
import com.eventeanagementsystem.event_management_system.db.OrganizerRepository;
import com.eventeanagementsystem.event_management_system.db.UserRole;
import com.eventeanagementsystem.event_management_system.dto.AuthResponse;
import com.eventeanagementsystem.event_management_system.dto.LoginRequest;
import com.eventeanagementsystem.event_management_system.dto.RegisterRequest;
import com.eventeanagementsystem.event_management_system.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final OrganizerRepository organizerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(AppUserRepository userRepository, OrganizerRepository organizerRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil){
        this.userRepository = userRepository;
        this.organizerRepository = organizerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request){
        // 1. Create the AppUser
        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        UserRole role = UserRole.valueOf(request.getRole().toUpperCase());
        user.setRole(role);
        AppUser savedUser = userRepository.save(user);

        // 2. If the role is ORGANIZER, create the associated Organizer entity
        if (role == UserRole.ORGANIZER) {
            Organizer organizer = new Organizer();
            // The name can be derived from the email or a dedicated field in RegisterRequest
            // For now, let's use the part of the email before the '@'
            organizer.setName(request.getEmail().split("@")[0]);
            organizer.setEmail(request.getEmail());
            organizer.setUser(savedUser);
            organizerRepository.save(organizer);
        }

        // 3. Generate a token for the new user and return it (auto-login)
        String token = jwtUtil.generateToken(savedUser.getEmail());
        return new AuthResponse(token, savedUser.getRole());
    }

    public AuthResponse login(LoginRequest request){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getRole());
    }
}
