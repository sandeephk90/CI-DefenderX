package com.cidefenderx.controller;

import com.cidefenderx.repository.UserRepository;
import com.cidefenderx.security.JwtService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        var userDetails = userDetailsService.loadUserByUsername(req.getUsername());
        var user = userRepository.findByUsername(req.getUsername()).orElseThrow();

        user.setLastLogin(OffsetDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(userDetails, Map.of(
                "role", user.getRole().name(),
                "userId", user.getId().toString()
        ));

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "fullName", user.getFullName() != null ? user.getFullName() : ""
        ));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
