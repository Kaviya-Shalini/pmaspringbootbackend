package com.example.personalmemory.controller;

import com.example.personalmemory.model.Alert;
import com.example.personalmemory.repository.AlertRepository;
import com.example.personalmemory.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.Instant;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertRepository alertRepository;
    private final NotificationService notificationService;

    public AlertController(AlertRepository alertRepository, NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
    }

    // POST /api/alerts/danger
    @PostMapping("/danger")
    public ResponseEntity<?> danger(@RequestBody Alert request) {
        request.setTimestamp(Instant.now());
        Alert saved = alertRepository.save(request);

        // notify family members (WebSocket + FCM if configured)
        notificationService.notifyFamilyMembers(request.getPatientId(), saved);

        return ResponseEntity.ok(Map.of("status", "ok", "message", "Alert queued/sent", "alert", saved));
    }
}
