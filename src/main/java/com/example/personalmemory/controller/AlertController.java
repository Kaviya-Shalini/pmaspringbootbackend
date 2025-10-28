package com.example.personalmemory.controller;

import com.example.personalmemory.model.Alert;
import com.example.personalmemory.service.FamilyService;
import com.example.personalmemory.service.LocationService; // Add LocationService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {


    // Key: PatientId, Value: The active Alert object
    private static final Map<String, Alert> activeDangerAlerts = new ConcurrentHashMap<>();

    @Autowired
    private FamilyService familyService;

    @Autowired // Inject LocationService
    private LocationService locationService;

    /**
     * Helper method to get all active, persistent alerts relevant to a family member's userId.
     */
    private List<Alert> getPersistentAlertsForFamilyUser(String userId) {
        // Find all active alerts and filter them to only those relevant to the current user.
        return activeDangerAlerts.values().stream()
                // Checks if the family member (userId) is connected to the patient who triggered the alert.
                .filter(alert -> familyService.getFamilyMembersByPatientId(alert.getPatientId()).contains(userId))
                .collect(Collectors.toList());
    }


    @PostMapping("/danger")
    public ResponseEntity<String> createAlert(@RequestBody Alert alert) {
        alert.setTimestamp(Instant.now());
        String patientId = alert.getPatientId();

        // 1. Check if patient is at permanent location (SAFE)
        boolean isSafe = locationService.isAtPermanentLocation(
                patientId,
                alert.getLatitude(),
                alert.getLongitude()
        );

        // 2. Check for connected family members
        List<String> connectedFamily = familyService.getFamilyMembersByPatientId(patientId);

        if (connectedFamily.isEmpty()) {
            return ResponseEntity.status(404).body("No family members connected for patient: " + patientId);
        }

        // 3. Logic: If patient is NOT safe, create/update the persistent alert.
        if (!isSafe) {
            // Patient is in DANGER and NOT at permanent location -> Activate persistent alert
            alert.setMessage("PERSISTENT DANGER! Patient is away from their safe location: ("
                    + String.format("%.4f", alert.getLatitude()) + ", "
                    + String.format("%.4f", alert.getLongitude()) + ")");
            activeDangerAlerts.put(patientId, alert);
            return ResponseEntity.ok("Persistent DANGER Alert activated for patient: " + patientId);
        }

        // 4. Logic: If patient is SAFE, clear any existing persistent alert.
        if (activeDangerAlerts.containsKey(patientId)) {
            activeDangerAlerts.remove(patientId);
            return ResponseEntity.ok("Persistent Alert RESOLVED. Patient is safe at permanent location: " + patientId);
        }

        // Case: No active alert and patient is safe -> just acknowledge
        return ResponseEntity.ok("Patient is safe. No persistent alert needed.");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Alert>> getAlertsForUser(@PathVariable String userId) {
        // Retrieve and return ONLY the persistent active alerts. NO CLEARING LOGIC.
        // This ensures the alert persists on the family member's dashboard.
        List<Alert> alerts = getPersistentAlertsForFamilyUser(userId);

        // The original logic to clear the alerts has been removed.
        // if (familyAlerts.containsKey(userId)) {
        //     familyAlerts.get(userId).clear();
        // }

        return ResponseEntity.ok(alerts);
    }
}