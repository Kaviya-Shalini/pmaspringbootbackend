package com.example.personalmemory.controller;

import com.example.personalmemory.model.Routine;
import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.UserRepository;
import com.example.personalmemory.service.FamilyService;
import com.example.personalmemory.service.NotificationService;
import com.example.personalmemory.service.RoutineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {

    @Autowired
    private RoutineService routineService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyService familyService;
    @Autowired
    private NotificationService notificationService;

    // Create a routine (only family member connected to patient can)
    @PostMapping("/create")
    public ResponseEntity<?> createRoutine(@RequestBody Map<String, Object> body) {
        String patientId = (String) body.get("patientId");
        String createdBy = (String) body.get("createdBy"); // familyMemberId
        String question = (String) body.get("question");
        String timeOfDay = (String) body.get("timeOfDay"); // "HH:mm"
        Boolean repeatDaily = (Boolean) body.getOrDefault("repeatDaily", Boolean.TRUE);

        if (patientId == null || createdBy == null || question == null || timeOfDay == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing fields"));
        }

        // check createdBy exists and is connected to patient
        Optional<User> familyMemberOpt = userRepository.findById(createdBy);
        if (familyMemberOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Creator not found"));
        }

        // check connection exists
        boolean connected = familyService.getConnectionsForFamilyMember(createdBy)
                .stream()
                .anyMatch(conn -> conn.getPatientId().equals(patientId));
        if (!connected) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Not connected to patient"));
        }

        Routine r = new Routine(patientId, createdBy, question, timeOfDay, repeatDaily);
        Routine saved = routineService.createRoutine(r);
        return ResponseEntity.ok(Map.of("success", true, "data", saved));
    }
    @GetMapping("/family/{familyMemberId}")
    public ResponseEntity<?> getRoutinesByFamily(@PathVariable String familyMemberId) {
        try {
            // get all patients connected to this family member
            var connections = familyService.getConnectionsForFamilyMember(familyMemberId);

            // get all routines for each connected patient
            List<Routine> allRoutines = connections.stream()
                    .flatMap(conn -> routineService.getRoutinesForPatient(conn.getPatientId()).stream())
                    .toList();

            return ResponseEntity.ok(allRoutines);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // list routines for patient (for both patient and family member views)
    @GetMapping("/forPatient/{patientId}")
    public ResponseEntity<?> getForPatient(@PathVariable String patientId) {
        List<Routine> list = routineService.getRoutinesForPatient(patientId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{routineId}")
    public ResponseEntity<?> deleteRoutine(@PathVariable String routineId, @RequestParam String requestedBy) {
        Optional<Routine> rOpt = routineService.findById(routineId);
        if (rOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Not found"));
        }

        Routine r = rOpt.get();

        // ✅ Allow both creator and connected patient to delete
        if (!r.getCreatedBy().equals(requestedBy) && !r.getPatientId().equals(requestedBy)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Not allowed to delete"));
        }

        routineService.deleteRoutine(routineId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Deleted"));
    }
    @GetMapping("/shared/{userId}")
    public ResponseEntity<?> getSharedRoutines(@PathVariable String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
        }

        User user = userOpt.get();
        List<Routine> routines;

        // 🧠 If Alzheimer = true → this is a patient
        if (user.isAlzheimer()) {
            // Get all active routines created for this patient
            routines = routineService.getRoutinesForPatient(user.getId());
        } else {
            // Otherwise, it’s a family member → fetch routines for all connected patients
            var connections = familyService.getConnectionsForFamilyMember(user.getId());
            routines = connections.stream()
                    .flatMap(conn -> routineService.getRoutinesForPatient(conn.getPatientId()).stream())
                    .toList();
        }

        return ResponseEntity.ok(routines);
    }
    @PostMapping("/add")
    public ResponseEntity<?> addRoutine(@RequestBody Routine routine) {
        Routine savedRoutine = routineService.saveRoutine(routine);

        // Notify connected family members or patients
        try {
            notificationService.sendRoutineNotification(savedRoutine);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(savedRoutine);
    }


}
