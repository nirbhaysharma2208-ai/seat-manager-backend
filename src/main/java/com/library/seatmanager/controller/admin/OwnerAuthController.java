package com.library.seatmanager.controller.admin;



import com.library.seatmanager.config.JwtUtil;


import com.library.seatmanager.dto.admin.OwnerLoginRequest;
import com.library.seatmanager.dto.admin.OwnerSignupRequest;
import com.library.seatmanager.entity.admin.Owner;
import com.library.seatmanager.repository.admin.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/owner-auth")
public class OwnerAuthController {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    private String configuredSignupKey;


    // OWNER SIGNUP
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody OwnerSignupRequest request) {

        if (request.getName() == null
                || request.getName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Name is required"));
        }

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is required"));
        }

        if (request.getPassword() == null
                || request.getPassword().length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Password must contain at least 8 characters"
                    ));
        }


        String email = request.getEmail()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

        if (ownerRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is already registered"));
        }

        Owner owner = new Owner();
        owner.setName(request.getName().trim());
        owner.setEmail(email);
        owner.setPassword(passwordEncoder.encode(request.getPassword()));
        owner.setActive(true);

        owner = ownerRepository.save(owner);

        String token = jwtUtil.generateOwnerToken(owner);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildAuthResponse(owner, token));
    }

    // OWNER LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody OwnerLoginRequest request) {

        if (request.getEmail() == null
                || request.getEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email and password are required"
                    ));
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

        Optional<Owner> optionalOwner =
                ownerRepository.findByEmail(email);

        if (optionalOwner.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        Owner owner = optionalOwner.get();

        if (!owner.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Owner account is inactive"));
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                owner.getPassword())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        String token = jwtUtil.generateOwnerToken(owner);

        return ResponseEntity.ok(buildAuthResponse(owner, token));
    }

    // CURRENT OWNER PROFILE
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String email = authentication.getName();

        Optional<Owner> optionalOwner =
                ownerRepository.findByEmail(email);

        if (optionalOwner.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Owner account not found"));
        }

        Owner owner = optionalOwner.get();

        if (!owner.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Owner account is inactive"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userType", "OWNER");
        response.put("userId", owner.getId());
        response.put("name", owner.getName());
        response.put("email", owner.getEmail());

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildAuthResponse(
            Owner owner,
            String token) {

        Map<String, Object> response = new HashMap<>();

        response.put("token", token);
        response.put("tokenType", "Bearer");
        response.put("userType", "OWNER");
        response.put("role", "OWNER");
        response.put("userId", owner.getId());
        response.put("name", owner.getName());
        response.put("email", owner.getEmail());

        return response;
    }
}
