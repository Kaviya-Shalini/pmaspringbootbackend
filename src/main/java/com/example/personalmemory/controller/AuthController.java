package com.example.personalmemory.controller;

import com.example.personalmemory.model.User;
import com.example.personalmemory.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            User user = authService.register(username, password);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account created successfully!",
                    "userId", user.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        boolean valid = authService.login(username, password);
        if (valid) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Login successful!"
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Invalid username or password"
            ));
        }
    }
}
