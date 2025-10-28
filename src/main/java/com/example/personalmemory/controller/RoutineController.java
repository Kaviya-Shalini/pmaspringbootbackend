package com.example.personalmemory.controller;

import com.example.personalmemory.model.RoutineTask;
import com.example.personalmemory.model.RoutineResponse;
import com.example.personalmemory.repository.RoutineTaskRepository;
import com.example.personalmemory.repository.RoutineResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/routine")
public class RoutineController {

    @Autowired
    private RoutineTaskRepository routineTaskRepository;

    @Autowired
    private RoutineResponseRepository routineResponseRepository;

    // --- Caregiver Endpoints (Set Routine) ---

    @PostMapping("/tasks")
    public ResponseEntity<RoutineTask> createRoutineTask(@RequestBody RoutineTask task) {
        // Simple validation that repeatDaily must be true for the current implementation
        if (!task.isRepeatDaily()) {
            // In a real app, you'd handle single-shot routines here, but for daily feature, we enforce it.
            task.setRepeatDaily(true);
        }
        RoutineTask savedTask = routineTaskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    @GetMapping("/tasks/patient/{patientId}")
    public ResponseEntity<List<RoutineTask>> getRoutineTasksForPatient(@PathVariable String patientId) {
        List<RoutineTask> tasks = routineTaskRepository.findByPatientId(patientId);
        return ResponseEntity.ok(tasks);
    }

    // --- Patient Endpoint (Record Response) ---

    @PostMapping("/response")
    public ResponseEntity<RoutineResponse> recordRoutineResponse(@RequestBody ResponseRequest request) {
        Optional<RoutineResponse> optionalResponse = routineResponseRepository.findById(request.responseId);

        if (optionalResponse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        RoutineResponse response = optionalResponse.get();

        // Ensure response is not recorded twice
        if (!"PENDING".equals(response.getResponse())) {
            return ResponseEntity.badRequest().body(response);
        }

        response.setResponse(request.response);
        // The time the button was clicked, sent from the frontend
        Instant patientResponseTime = request.responseTimestamp;
        response.setResponseTimestamp(patientResponseTime);

        // Calculate Time To Respond (ACRS Innovative Feature)
        long timeDiffMs = ChronoUnit.MILLIS.between(response.getNotificationTimestamp(), patientResponseTime);
        response.setTimeToRespondMs(timeDiffMs);

        RoutineResponse updatedResponse = routineResponseRepository.save(response);

        // In a real app, you might also push a notification to the caregiver here (Caregiver dashboard update)

        return ResponseEntity.ok(updatedResponse);
    }

    // DTO for incoming patient response
    public static class ResponseRequest {
        public String responseId;
        public String response; // "YES" or "NO"
        public Instant responseTimestamp;

        // Getters/Setters omitted for brevity but required for Spring serialization
    }

    // --- Caregiver Dashboard Endpoint (View Data) ---

    @GetMapping("/responses/task/{taskId}")
    public ResponseEntity<List<RoutineResponse>> getTaskHistory(@PathVariable String taskId) {
        List<RoutineResponse> history = routineResponseRepository.findByTaskIdOrderByResponseTimestampDesc(taskId);
        return ResponseEntity.ok(history);
    }
}