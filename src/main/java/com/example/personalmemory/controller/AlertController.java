package com.example.personalmemory.controller;

import com.example.personalmemory.model.Alert;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @PostMapping("/danger")
    public ResponseEntity<?> danger(@RequestBody Alert alert) {
        // In a real application, you would process this alert (e.g., send a notification)
        System.out.println("Received DANGER alert for patient: " + alert.getPatientId() + " at lat: " + alert.getLatitude());
        return ResponseEntity.ok().build();
    }
}