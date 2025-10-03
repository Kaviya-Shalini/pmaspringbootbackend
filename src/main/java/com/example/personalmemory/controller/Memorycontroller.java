package com.example.personalmemory.controller;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.service.MemoryService;
import com.example.personalmemory.repository.MemoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Memorycontroller {

    private final MemoryService memoryService;
    private final MemoryRepository memoryRepository;
    private final PatientStatusRepository patientStatusRepository;

    public Memorycontroller(MemoryService memoryService,
                            MemoryRepository memoryRepository,
                            PatientStatusRepository patientStatusRepository) {
        this.memoryService = memoryService;
        this.memoryRepository = memoryRepository;
        this.patientStatusRepository = patientStatusRepository;
    }

    // Endpoint 1: patient status
    @PostMapping("/patient-status")
    public ResponseEntity<?> savePatientStatus(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "user-unknown");
        Boolean isAlzheimer = (Boolean) body.getOrDefault("isAlzheimer", false);

        PatientStatus saved = memoryService.saveOrUpdatePatientStatus(userId, isAlzheimer);
        return ResponseEntity.ok(Map.of("success", true, "message", "Patient status saved", "data", saved));
    }

    // Endpoint 2: memories upload (multipart)
    @PostMapping(value = "/memories", consumes = { "multipart/form-data" })
    public ResponseEntity<?> uploadMemory(
            @RequestPart("data") Addmemory data,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "voiceNote", required = false) MultipartFile voiceNote) {

        Addmemory saved = memoryService.saveMemory(data, file, voiceNote);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "message", "Memory saved", "data", saved));
    }

    // Optional: Get memory by id
    @GetMapping("/memories/{id}")
    public ResponseEntity<?> getMemory(@PathVariable String id) {
        Addmemory mem = memoryService.getMemory(id);
        return ResponseEntity.ok(Map.of("success", true, "data", mem));
    }
}
