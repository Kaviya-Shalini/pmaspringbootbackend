package com.example.personalmemory.controller;

import com.example.personalmemory.model.User;
import com.example.personalmemory.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        try {
            String username = (String) body.get("username");
            String password = (String) body.get("password");
            boolean isAlzheimer = (boolean) body.get("isAlzheimer");

            User user = authService.register(username, password, isAlzheimer);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account created successfully!",
                    "userId", user.getId(),
                    "quickQuestionAnswered", user.isQuickQuestionAnswered()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        Optional<User> userOpt = authService.login(body.get("username"), body.get("password"));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Login successful!",
                    "userId", user.getId(),
                    "username", user.getUsername(), // Return username on login
                    "quickQuestionAnswered", user.isQuickQuestionAnswered()
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid credentials"));
        }
    }

    @PostMapping("/register-face")
    public ResponseEntity<?> registerFace(
            @RequestParam("userId") String userId,
            @RequestParam("face") MultipartFile faceImage) {
        try {
            authService.registerFace(userId, faceImage);
            return ResponseEntity.ok(Map.of("success", true, "message", "Face registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/login-face")
    public ResponseEntity<?> loginWithFace(
            @RequestParam("face") MultipartFile faceImage,
            @RequestParam("username") String username) { // <-- MODIFIED: Accept username
        try {
            // Pass both face and username to the service
            var userOpt = authService.loginWithFace(faceImage, username);
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Face login successful",
                        "userId", user.getId(),
                        "username", user.getUsername(), // Return username on login
                        "quickQuestionAnswered", user.isQuickQuestionAnswered()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Face not recognized or does not match the username."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/user/delete/{userId}") // <-- Final endpoint structure
    public ResponseEntity<?> deleteAccount(@PathVariable String userId) {
        boolean deleted = authService.deleteUser(userId);
        if (deleted)
            return ResponseEntity.ok(Map.of("success", true, "message", "User and all related data deleted"));
        else
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "User not found"));
    }
}
