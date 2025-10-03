package com.example.personalmemory.controller;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.UserRepository;
import com.example.personalmemory.service.MemoryService;
import com.example.personalmemory.repository.MemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Memorycontroller {
    // Save/update Alzheimer status
    @Autowired
    private MemoryService memoryService;
    @PostMapping("/patient-status")
    public ResponseEntity<?> savePatientStatus(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "user-unknown");
        Boolean isAlzheimer = (Boolean) body.getOrDefault("isAlzheimer", false);

        User saved = memoryService.savePatientStatus(userId, isAlzheimer);
        return ResponseEntity.ok(Map.of("success", true, "message", "Patient status saved", "data", saved));
    }

    // Get Alzheimer status
    @GetMapping("/patient-status/{userId}")
    public ResponseEntity<?> getPatientStatus(@PathVariable String userId) {
        Boolean status = memoryService.getPatientStatus(userId);
        return ResponseEntity.ok(Map.of("success", true, "userId", userId, "isAlzheimer", status));
    }

}
