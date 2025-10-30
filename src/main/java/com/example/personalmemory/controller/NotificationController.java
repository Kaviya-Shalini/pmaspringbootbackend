package com.example.personalmemory.controller;

import com.example.personalmemory.model.Routine;
import com.example.personalmemory.model.RoutineResponse;
import com.example.personalmemory.repository.RoutineRepository;
import com.example.personalmemory.repository.RoutineResponseRepository;
import com.example.personalmemory.service.RoutineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private RoutineResponseRepository responseRepository;

    @Autowired
    private RoutineService routineService;

    @PostMapping("/answer")
    public ResponseEntity<?> answerRoutine(@RequestBody Map<String, String> body) {
        String routineId = body.get("routineId");
        String patientId = body.get("patientId");
        String answer = body.get("answer"); // "yes" or "no"

        if (routineId == null || patientId == null || answer == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing fields"));
        }

        Optional<Routine> routineOpt = routineService.findById(routineId);
        if (routineOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Routine not found"));
        }
        Routine r = routineOpt.get();
        RoutineResponse rr = new RoutineResponse(routineId, patientId, answer, Instant.now(), r.getCreatedBy());
        responseRepository.save(rr);

        return ResponseEntity.ok(Map.of("success", true, "message", "Saved"));
    }
}
