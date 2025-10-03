package com.example.personalmemory.controller;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.model.User;
import com.example.personalmemory.service.MemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
            @RequestParam(value = "reminderAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date reminderAt,
            @RequestParam("reminderDaily") boolean reminderDaily,
            @RequestParam(value = "medicationName", required = false) String medicationName,
            @RequestParam(value = "dosage", required = false) String dosage,
            @RequestParam(value = "storageLocation", required = false) String storageLocation
    ) throws IOException { // Let the method throw the exception

        Addmemory memory = new Addmemory();
        memory.setUserId(userId);
        memory.setTitle(title);
        memory.setCategory(category);
        memory.setCustomCategory(customCategory);
        memory.setDescription(description);
        memory.setReminderAt(reminderAt);
        memory.setReminderDaily(reminderDaily);
        memory.setMedicationName(medicationName);
        memory.setDosage(dosage);
        memory.setStorageLocation(storageLocation);

        Addmemory savedMemory = memoryService.createMemory(memory, file, voiceNote);

        return ResponseEntity.ok(Map.of("success", true, "message", "Memory uploaded successfully!", "data", savedMemory));
    }
}