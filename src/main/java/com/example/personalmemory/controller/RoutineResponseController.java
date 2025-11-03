package com.example.personalmemory.controller;

import com.example.personalmemory.model.RoutineResponse;
import com.example.personalmemory.service.RoutineResponseService;
import com.example.personalmemory.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/routineResponses")
public class RoutineResponseController {

    @Autowired
    private RoutineResponseService responseService;

    @Autowired
    private NotificationService notificationService;

    // Alzheimer patient clicks Yes/No
    @PostMapping("/respond")
    public ResponseEntity<?> recordResponse(@RequestBody Map<String, String> body) {
        String routineId = body.get("routineId");
        String patientId = body.get("patientId");
        String createdBy = body.get("createdBy");
        String answer = body.get("answer");

        if (routineId == null || patientId == null || answer == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing fields"));
        }

        RoutineResponse saved = responseService.recordResponse(routineId, patientId, createdBy, answer);

        // Optionally notify the family member about the response
        try {
            notificationService.sendRoutineResponseNotification(saved);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(Map.of("success", true, "data", saved));
    }

    // Fetch latest response for each routine (both family and patient can view)
    @GetMapping("/{routineId}/latest")
    public ResponseEntity<?> getLatestResponse(@PathVariable String routineId) {
        Optional<RoutineResponse> response = responseService.getLatestResponseForRoutine(routineId);
        if (response.isEmpty()) {
            return ResponseEntity.ok(Map.of("answered", false, "message", "No response yet"));
        }
        return ResponseEntity.ok(response.get());
    }
}
