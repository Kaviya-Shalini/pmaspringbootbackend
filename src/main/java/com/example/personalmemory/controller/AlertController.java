package com.example.personalmemory.controller;

import com.example.personalmemory.model.Alert;
import com.example.personalmemory.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private FamilyService familyService;


    @PostMapping("/danger")
    public ResponseEntity<String> createAlert(@RequestBody Alert alert) {
        alert.setTimestamp(Instant.now());
        String patientId = alert.getPatientId();

        // Dynamically find connected family members from the database
        List<String> connectedFamily = familyService.getFamilyMembersByPatientId(patientId);

        if (connectedFamily.isEmpty()) {
            return ResponseEntity.status(404).body("No family members connected for patient: " + patientId);
        }

        for (String familyId : connectedFamily) {
            familyAlerts.computeIfAbsent(familyId, k -> new ArrayList<>()).add(alert);
        }
        return ResponseEntity.ok("Alert sent to family of patient: " + patientId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Alert>> getAlertsForUser(@PathVariable String userId) {
        List<Alert> alerts = new ArrayList<>(familyAlerts.getOrDefault(userId, Collections.emptyList()));
        // After fetching, clear the list for that user so the same alert won’t show again
        if (familyAlerts.containsKey(userId)) {
            familyAlerts.get(userId).clear();
        }
        return ResponseEntity.ok(alerts);
    }
}
