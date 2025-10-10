package com.example.personalmemory.controller;

import com.example.personalmemory.model.EmergencyContact;
import com.example.personalmemory.service.EmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/emergencycontacts")
public class EmergencyContactController {

    private final EmergencyContactService svc;

    @Autowired
    public EmergencyContactController(EmergencyContactService svc) {
        this.svc = svc;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addContact(
            @RequestPart("userId") String userId,
            @RequestPart("name") String name,
            @RequestPart("relationship") String relationship,
            @RequestPart("phone") String phone,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            EmergencyContact saved = svc.addContact(userId, name, relationship, phone, photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to store contact photo"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateContact(
            @PathVariable String id,
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "relationship", required = false) String relationship,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            return svc.updateContact(id, name, relationship, phone, photo)
                    .map(c -> ResponseEntity.ok(mapToResponse(c)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Contact not found")));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update contact"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String q
    ) {
        Page<EmergencyContact> p = svc.getContacts(page, size, q);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", p.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()));
        resp.put("total", p.getTotalElements());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/photo/{fileId}")
    public ResponseEntity<?> getPhoto(@PathVariable String fileId) {
        try {
            var opt = svc.getPhotoResource(fileId);
            if (opt.isEmpty()) return ResponseEntity.notFound().build();

            GridFsResource res = opt.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    res.getContentType() != null ? MediaType.parseMediaType(res.getContentType()) : MediaType.APPLICATION_OCTET_STREAM
            );
            headers.setContentLength(res.contentLength());
            return new ResponseEntity<>(new InputStreamResource(res.getInputStream()), headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Cannot read file"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable String id) {
        svc.deleteContact(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> mapToResponse(EmergencyContact c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("relationship", c.getRelationship());
        m.put("phone", c.getPhone());
        m.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : Instant.now().toString());
        String baseUrl = "http://localhost:8080";
        m.put("photoUrl", c.getPhotoFileId() != null ? baseUrl + "/api/emergencycontacts/photo/" + c.getPhotoFileId() : null);
        return m;
    }

    @GetMapping("/emergency/{userId}")
    public ResponseEntity<?> getContactsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String q
    ) {
        Page<EmergencyContact> p = svc.getContactsByUser(userId, page, size, q);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", p.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()));
        resp.put("total", p.getTotalElements());
        resp.put("page", p.getNumber());
        resp.put("size", p.getSize());
        return ResponseEntity.ok(resp);
    }
}
