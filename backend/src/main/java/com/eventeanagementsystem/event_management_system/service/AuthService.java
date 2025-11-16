package com.eventeanagementsystem.event_management_system.service;

import com.eventeanagementsystem.event_management_system.db.AppUser;
import com.eventeanagementsystem.event_management_system.db.AppUserRepository;
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

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest request){
        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.valueOf(request.getRole()));
        userRepository.save(user);
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
