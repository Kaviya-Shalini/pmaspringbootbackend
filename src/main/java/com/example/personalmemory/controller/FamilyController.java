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
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody Map<String, String> body) {
        String patientId = body.get("patientId");
        String familyMemberUsername = body.get("familyMemberUsername");

        if (patientId == null || familyMemberUsername == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing patientId or familyMemberUsername"));
        }

        Optional<User> familyMemberOpt = userRepository.findByUsername(familyMemberUsername);
        if (familyMemberOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Family member user not found"));
        }

        User familyMember = familyMemberOpt.get();
        FamilyConnection created = familyService.createConnection(patientId, familyMember.getId());

        if (created == null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Already connected"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Family member connected", "data", created));
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> listConnections(@PathVariable String userId) {
        User currentUser = userRepository.findById(userId).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
        }

        List<String> connectedUserIds;
        if (currentUser.isAlzheimer()) {
            // Patient is logged in, find their family members
            connectedUserIds = familyService.getFamilyMembersByPatientId(currentUser.getId());
        } else {
            // Family member is logged in, find the patients they are connected to
            List<FamilyConnection> connections = familyService.getConnectionsForFamilyMember(currentUser.getId());
            connectedUserIds = connections.stream().map(FamilyConnection::getPatientId).collect(Collectors.toList());
        }

        List<User> connectedUsers = (List<User>) userRepository.findAllById(connectedUserIds);

        List<Map<String, String>> out = connectedUsers.stream()
                .map(u -> Map.of("id", u.getId(), "username", u.getUsername()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(out);
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestBody Map<String, String> body) {
        String patientId = body.get("patientId");
        String familyMemberId = body.get("familyMemberId");

        if (patientId == null || familyMemberId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing patientId or familyMemberId"));
        }

        familyService.disconnect(patientId, familyMemberId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Disconnected"));
    }
}
