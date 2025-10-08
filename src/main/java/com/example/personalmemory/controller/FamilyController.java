package com.example.personalmemory.controller;

import com.example.personalmemory.model.FamilyConnection;
import com.example.personalmemory.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    // Connect family member
    // Expects header "X-Username" = OWNER username or you can pass userId as body
    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestHeader(value = "X-Username", required = false) String ownerUsername,
                                     @RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", ownerUsername);
        String usernameToConnect = body.get("username");
        if (userId == null || usernameToConnect == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing userId or username to connect"));
        }

        FamilyConnection created = familyService.connect(userId, usernameToConnect);
        if (created == null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Already connected", "data", Map.of("username", usernameToConnect)));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Family member connected", "data", created));
    }

    // List connected family members for a given userId
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "X-Username", required = false) String ownerUsername,
                                  @RequestParam(value = "userId", required = false) String userIdParam) {
        String userId = userIdParam != null ? userIdParam : ownerUsername;
        if (userId == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing user identifier"));

        List<FamilyConnection> list = familyService.listForUser(userId);
        // transform to minimal info expected by client
        List<Map<String, String>> out = list.stream().map(fc -> Map.of("id", fc.getId(), "username", fc.getFamilyUsername())).toList();
        return ResponseEntity.ok(out);
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestHeader(value = "X-Username", required = false) String ownerUsername,
                                        @RequestBody Map<String, String> body) {
        String userId = body.getOrDefault("userId", ownerUsername);
        String username = body.get("username");
        if (userId == null || username == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing userId or username"));
        }
        familyService.disconnect(userId, username);
        return ResponseEntity.ok(Map.of("success", true, "message", "Disconnected"));
    }
}