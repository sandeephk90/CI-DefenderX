package com.cidefenderx.controller;

import com.cidefenderx.repository.UserRepository;
import com.cidefenderx.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authManager, JwtService jwtService,
                          UserDetailsService userDetailsService, UserRepository userRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username, req.password));

        var userDetails = userDetailsService.loadUserByUsername(req.username);
        var user = userRepository.findByUsername(req.username).orElseThrow();

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

    public static class LoginRequest {
        public String username;
        public String password;
    }
}
