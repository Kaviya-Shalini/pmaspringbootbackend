package com.example.personalmemory.controller;

import com.example.personalmemory.model.PhotoContact;
import com.example.personalmemory.service.PhotoContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.gridfs.GridFsResource;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photocontacts") // change to your frontend origin or use a global CORS config
public class PhotoContactController {

    private final PhotoContactService svc;

    @Autowired
    public PhotoContactController(PhotoContactService svc) {
        this.svc = svc;
    }

    // Create contact
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addContact(
            @RequestPart("name") String name,
            @RequestPart("relationship") String relationship,
            @RequestPart("phone") String phone,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            PhotoContact saved = svc.addContact(name, relationship, phone, photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to store file"));
        }
    }

    // Update contact
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateContact(
            @PathVariable String id,
            @RequestPart(value="name", required = false) String name,
            @RequestPart(value="relationship", required = false) String relationship,
            @RequestPart(value="phone", required = false) String phone,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            return svc.updateContact(id, name, relationship, phone, photo)
                    .map(c -> ResponseEntity.ok(mapToResponse(c)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Contact not found")));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to store file"));
        }
    }

    // Pagination + search
    @GetMapping
    public ResponseEntity<?> getContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String q
    ) {
        Page<PhotoContact> p = svc.getContacts(page, size, q);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", p.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()));
        resp.put("total", p.getTotalElements());
        resp.put("page", p.getNumber());
        resp.put("size", p.getSize());
        return ResponseEntity.ok(resp);
    }

    // Serve photo by GridFS id
    @GetMapping("/photo/{fileId}")
    public ResponseEntity<?> getPhoto(@PathVariable String fileId) {
        try {
            var opt = svc.getPhotoResource(fileId);
            if (opt.isEmpty()) return ResponseEntity.notFound().build();

            GridFsResource res = opt.get();
            HttpHeaders headers = new HttpHeaders();
            if (res.getContentType() != null) headers.setContentType(MediaType.parseMediaType(res.getContentType()));
            else headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(res.contentLength());
            return new ResponseEntity<>(new InputStreamResource(res.getInputStream()), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file id"));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Cannot read file"));
        }
    }

    // Delete contact
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable String id) {
        svc.deleteContact(id);
        return ResponseEntity.noContent().build();
    }

    // Helper to map PhotoContact to shape frontend expects
    private Map<String,Object> mapToResponse(PhotoContact c) {
        Map<String,Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("relationship", c.getRelationship());
        m.put("phone", c.getPhone());
        m.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : Instant.now().toString());
        if (c.getPhotoFileId() != null) {
            // This URL matches the Angular usage of photoUrl
            m.put("photoUrl", "/api/photocontacts/photo/" + c.getPhotoFileId());
        } else {
            m.put("photoUrl", null);
        }
        return m;
    }
}
