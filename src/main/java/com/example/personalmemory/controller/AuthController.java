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
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            User user = authService.register(username, password);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account created successfully!",
                    "userId", user.getId(),
                    "quickQuestionAnswered", user.isQuickQuestionAnswered()
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

        // Use the updated service method
        Optional<User> userOpt = authService.login(username, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Login successful!",
                    "userId", user.getId(), // Add userId to the response
                    "quickQuestionAnswered", user.isQuickQuestionAnswered()
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Invalid username or password"
            ));
        }
    }

    @PostMapping("/register-face")
    public ResponseEntity<?> registerFace(
            @RequestParam("userId") String userId,
            @RequestParam("face") MultipartFile faceImage) {
        try {
            authService.registerFace(userId, faceImage);
            return ResponseEntity.ok().body(Map.of("success", true, "message", "Face registered successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/login-face")
    public ResponseEntity<?> loginWithFace(@RequestParam("face") MultipartFile faceImage) {
        try {
            var userOpt = authService.loginWithFace(faceImage);
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Face login successful",
                        "userId", user.getId(),
                        "quickQuestionAnswered", user.isQuickQuestionAnswered()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Face not recognized"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteAccount(@PathVariable String userId) {
        boolean deleted = authService.deleteUser(userId);
        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User account and all related data deleted successfully"
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User not found"
            ));
        }
    }
}