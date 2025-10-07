package com.example.personalmemory.controller;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.model.DecryptedFile;
import com.example.personalmemory.model.User;
import com.example.personalmemory.service.MemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
            @RequestParam(value = "description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "voiceNote", required = false) MultipartFile voiceNote,
            @RequestParam(value = "reminderAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date reminderAt,
            @RequestParam("reminderDaily") boolean reminderDaily,
            @RequestParam(value = "medicationName", required = false) String medicationName,
            @RequestParam(value = "dosage", required = false) String dosage,
            @RequestParam(value = "storageLocation", required = false) String storageLocation
    ) { // Let the method throw the exception
        try {
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/memories/{memoryId}/download")
    public ResponseEntity<byte[]> downloadMemoryFile(
            @PathVariable String memoryId,
            @RequestParam("type") String type
    ) {
        try {
            DecryptedFile decryptedFile = memoryService.getDecryptedMemoryFile(memoryId, type);

            HttpHeaders headers = new HttpHeaders();
            String filename = decryptedFile.getFilename();

            // Detect content type based on file extension
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (filename.endsWith(".mp3")) mediaType = MediaType.valueOf("audio/mpeg");
            else if (filename.endsWith(".wav")) mediaType = MediaType.valueOf("audio/wav");
            else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) mediaType = MediaType.IMAGE_JPEG;
            else if (filename.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
            else if (filename.endsWith(".pdf")) mediaType = MediaType.APPLICATION_PDF;

            headers.setContentType(mediaType);
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(decryptedFile.getData(), headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("Error downloading file: " + e.getMessage()).getBytes());
        }
    }


    // ✅ Get memories (with pagination & search)
    @GetMapping("/memories")
    public ResponseEntity<?> getMemories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Addmemory> memories = memoryService.getAllMemories(pageable, search);
        return ResponseEntity.ok(memories);
    }

    // ✅ DELETE a memory (and its associated files)
    @DeleteMapping("/memories/{memoryId}")
    public ResponseEntity<?> deleteMemory(@PathVariable String memoryId) {
        try {
            memoryService.deleteMemory(memoryId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Memory and associated files deleted successfully!"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    // Get memories by userId with pagination and optional search
    @GetMapping("/memories/user/{userId}")
    public ResponseEntity<?> getMemoriesByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        // Reuse MemoryService.getAllMemories but filter by userId
        Pageable pageable = PageRequest.of(page, size);
        Page<Addmemory> memories;
        if (search == null || search.isBlank()) {
            memories = memoryService.getAllMemories(PageRequest.of(page, size), null)
                    .map(m -> m) // placeholder - we'll use repository method to filter by user
            ;
            // use repository method for user-specific
            memories = memoryService.getAllMemoriesByUser(userId, pageable, search);
        } else {
            memories = memoryService.getAllMemoriesByUser(userId, pageable, search);
        }
        return ResponseEntity.ok(Map.of(
                "content", memories.getContent(),
                "page", memories.getNumber(),
                "size", memories.getSize(),
                "totalPages", memories.getTotalPages(),
                "totalElements", memories.getTotalElements()
        ));
    }


}