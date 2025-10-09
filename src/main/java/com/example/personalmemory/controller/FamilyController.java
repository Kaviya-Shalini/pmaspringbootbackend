// in kaviya-shalini/pmaspringbootbackend/pmaspringbootbackend-7fe149d3a1ed8327691014420d2b6aba8592c29e/src/main/java/com/example/personalmemory/controller/FamilyController.java
package com.example.personalmemory.controller;

import com.example.personalmemory.model.FamilyConnection;
import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.UserRepository;
import com.example.personalmemory.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private UserRepository userRepository; // Inject UserRepository

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestHeader(value = "X-Username", required = false) String ownerUsername,
                                     @RequestBody Map<String, String> body) {
        String usernameToConnect = body.get("username");

        if (ownerUsername == null || usernameToConnect == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing username information"));
        }

        // Find the owner user by username to get their actual ID
        User owner = userRepository.findByUsername(ownerUsername)
                .orElse(null);
        if (owner == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Owner user not found"));
        }

        FamilyConnection created = familyService.connect(owner.getId(), usernameToConnect);
        if (created == null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Already connected"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Family member connected", "data", created));
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "X-Username", required = false) String ownerUsername) {
        if (ownerUsername == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing user identifier"));
        }

        User currentUser = userRepository.findByUsername(ownerUsername).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
        }

        List<User> connectedUsers;
        if (currentUser.isAlzheimer()) {
            // Patient is logged in, find their family members
            List<FamilyConnection> connections = familyService.listForUser(currentUser.getId());
            List<String> usernames = connections.stream().map(FamilyConnection::getFamilyUsername).toList();
            connectedUsers = userRepository.findAll().stream()
                    .filter(u -> usernames.contains(u.getUsername())).toList();
        } else {
            // Family member is logged in, find the patients they are connected to
            List<FamilyConnection> connections = familyService.listConnectionsForFamilyMember(currentUser.getUsername());
            List<String> userIds = connections.stream().map(FamilyConnection::getUserId).toList();
            connectedUsers = userRepository.findAllById(userIds);
        }

        // Return a clean list of user objects
        List<Map<String, String>> out = connectedUsers.stream()
                .map(u -> Map.of("id", u.getId(), "username", u.getUsername()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(out);
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestHeader(value = "X-Username", required = false) String ownerUsername,
                                        @RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String username = body.get("username");

        if (userId == null || username == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing userId or username"));
        }

        familyService.disconnect(userId, username);
        return ResponseEntity.ok(Map.of("success", true, "message", "Disconnected"));
    }

}