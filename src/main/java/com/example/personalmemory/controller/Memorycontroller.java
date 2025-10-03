package com.example.personalmemory.controller;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.model.User;
import com.example.personalmemory.service.MemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Memorycontroller {

    @Autowired
    private MemoryService memoryService;

    @PostMapping("/patient-status")
    public ResponseEntity<?> savePatientStatus(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "user-unknown");
        Boolean isAlzheimer = (Boolean) body.getOrDefault("isAlzheimer", false);

        User saved = memoryService.savePatientStatus(userId, isAlzheimer);
        return ResponseEntity.ok(Map.of("success", true, "message", "Patient status saved", "data", saved));
    }

    @GetMapping("/patient-status/{userId}")
    public ResponseEntity<?> getPatientStatus(@PathVariable String userId) {
        Boolean status = memoryService.getPatientStatus(userId);
        return ResponseEntity.ok(Map.of("success", true, "userId", userId, "isAlzheimer", status));
    }

    // ** NEW ENDPOINT TO ADD MEMORIES **
    @PostMapping("/memories")
    public ResponseEntity<?> createMemory(
            @RequestParam("userId") String userId,
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam(value = "customCategory", required = false) String customCategory,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "voiceNote", required = false) MultipartFile voiceNote,
            @RequestParam(value = "reminderAt", required = false) String reminderAt,
            @RequestParam("reminderDaily") boolean reminderDaily,
            @RequestParam(value = "medicationName", required = false) String medicationName,
            @RequestParam(value = "dosage", required = false) String dosage,
            @RequestParam(value = "storageLocation", required = false) String storageLocation,
            @RequestParam("isAlzheimer") boolean isAlzheimer
    ) {
        try {
            Addmemory memory = new Addmemory();
            memory.setUserId(userId);
            memory.setTitle(title);
            memory.setCategory(category);
            memory.setCustomCategory(customCategory);
            memory.setDescription(description);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy, hh:mm:ss a");

            if (reminderAt != null && !reminderAt.isEmpty()) {
                LocalDateTime ldt = LocalDateTime.parse(reminderAt, formatter);
                memory.setReminderAt(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
            }
            memory.setReminderDaily(reminderDaily);
            memory.setMedicationName(medicationName);
            memory.setDosage(dosage);
            memory.setStorageLocation(storageLocation);
            memory.setAlzheimer(isAlzheimer);

            Addmemory savedMemory = memoryService.createMemory(memory, file, voiceNote);

            return ResponseEntity.ok(Map.of("success", true, "message", "Memory uploaded successfully!", "data", savedMemory));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to upload file."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}