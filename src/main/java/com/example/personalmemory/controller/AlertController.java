package com.example.personalmemory.controller;

import com.example.personalmemory.model.Alert;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    // Map of userId (family member) → alerts list
    private final Map<String, List<Alert>> familyAlerts = new ConcurrentHashMap<>();

    // Example mapping of patient → connected family
    private final Map<String, List<String>> familyConnections = new HashMap<>();

    public AlertController() {
        // Example mapping; replace with DB later
        familyConnections.put("patient1", Arrays.asList("family1", "family2"));
        familyConnections.put("patient2", Collections.singletonList("family3"));
    }

    @PostMapping("/danger")
    public ResponseEntity<String> createAlert(@RequestBody Alert alert) {
        alert.setTimestamp(Instant.now());
        String patientId = alert.getPatientId();

        // Send to all connected family members
        List<String> connectedFamily = familyConnections.getOrDefault(patientId, new ArrayList<>());
        for (String familyId : connectedFamily) {
            familyAlerts.computeIfAbsent(familyId, k -> new ArrayList<>()).add(alert);
        }
        return ResponseEntity.ok("Alert sent to family of patient: " + patientId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Alert>> getAlertsForUser(@PathVariable String userId) {
        List<Alert> alerts = familyAlerts.getOrDefault(userId, new ArrayList<>());
        // After fetching, clear so the same alert won’t show again
        familyAlerts.remove(userId);
        return ResponseEntity.ok(alerts);
    }
}
