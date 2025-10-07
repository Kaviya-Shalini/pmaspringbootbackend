package com.example.personalmemory.controller;

import com.example.personalmemory.model.PhotoEntry;
import com.example.personalmemory.service.PhotoEntryService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mypeople")
@CrossOrigin(origins = "http://localhost:4200") // adjust as needed
public class PhotoEntryController {

    private final PhotoEntryService service;

    @Autowired
    public PhotoEntryController(PhotoEntryService service) {
        this.service = service;
    }

    // Add photo (multipart/form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addPhoto(
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) MultipartFile photo
    ) {
        try {
            PhotoEntry saved = service.addPhoto(caption, photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to store file"));
        }
    }

    // Update photo (multipart/form-data)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePhoto(
            @PathVariable String id,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) MultipartFile photo
    ) {
        try {
            Optional<PhotoEntry> updated = service.updatePhoto(id, caption, photo);
            return updated.map(entry -> ResponseEntity.ok(toDto(entry)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found")));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to store file"));
        }
    }

    // Delete photo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String id) {
        service.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    // Get photos with pagination & search
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPhotos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String search
    ) {
        Page<PhotoEntry> p = service.getPhotos(page, size, search);

        List<Map<String, Object>> items = p.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("data", items);
        response.put("total", p.getTotalElements());
        response.put("page", p.getNumber());
        response.put("size", p.getSize());

        return ResponseEntity.ok(response);
    }


    // Serve photo by GridFS id
    @GetMapping("/photo/{fileId}")
    public ResponseEntity<InputStreamResource> getPhoto(@PathVariable String fileId) {
        Optional<GridFsResource> maybe = service.getPhotoResource(fileId);

        if (maybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GridFsResource resource = maybe.get();
        try {
            String contentType = resource.getContentType();
            MediaType mediaType = (contentType != null && !contentType.isBlank())
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;

            InputStreamResource body = new InputStreamResource(resource.getInputStream());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);

            try {
                headers.setContentLength(resource.contentLength());
            } catch (IOException ignored) {
                // If content length can't be determined, skip setting it.
            }

            return new ResponseEntity<>(body, headers, HttpStatus.OK);
        } catch (IOException e) {
            // reading the stream failed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper: convert PhotoEntry -> DTO map (avoid mutating domain)
    private Map<String, Object> toDto(PhotoEntry e) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getId());
        m.put("caption", e.getCaption());
        m.put("createdAt", e.getCreatedAt() != null ? e.getCreatedAt().toString() : Instant.now().toString());
        if (e.getPhotoFileId() != null) {
            // return URL that frontend can use directly
            m.put("photoUrl", "/api/mypeople/photo/" + e.getPhotoFileId());
        } else {
            m.put("photoUrl", null);
        }
        return m;
    }
}
