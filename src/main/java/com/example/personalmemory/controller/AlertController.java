package com.example.personalmemory.controller;

import com.example.personalmemory.model.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public AlertController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/danger")
    public ResponseEntity<?> danger(@RequestBody Alert alert) {
        // In a real application, you would process this alert (e.g., send a notification)
        System.out.println("Received DANGER alert for patient: " + alert.getPatientId() + " at lat: " + alert.getLatitude());

        // Send the alert to the WebSocket topic so the dashboard can receive it
        messagingTemplate.convertAndSend("/topic/alerts", alert);

        return ResponseEntity.ok().build();
    }
}