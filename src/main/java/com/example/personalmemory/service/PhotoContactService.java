package com.example.personalmemory.service;

import com.example.personalmemory.model.PhotoContact;
import com.example.personalmemory.repository.PhotoContactRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import com.mongodb.client.gridfs.model.GridFSFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Service
public class PhotoContactService {

    private final PhotoContactRepository repo;
    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public PhotoContactService(PhotoContactRepository repo, GridFsTemplate gridFsTemplate) {
        this.repo = repo;
        this.gridFsTemplate = gridFsTemplate;
    }

    public PhotoContact addContact(String userId, String name, String relationship, String phone, MultipartFile photo) throws IOException { // <-- Add userId
        PhotoContact p = new PhotoContact();
        p.setUserId(userId); // <-- Set the userId
        p.setName(name.trim());
        p.setRelationship(relationship.trim());
        p.setPhone(phone.trim());
        p.setCreatedAt(Instant.now());

        if (photo != null && !photo.isEmpty()) {
            ObjectId fileId = gridFsTemplate.store(photo.getInputStream(), photo.getOriginalFilename(), photo.getContentType());
            p.setPhotoFileId(fileId.toHexString());
        }

        return repo.save(p);
    }

    public Optional<PhotoContact> updateContact(String id, String name, String relationship, String phone, MultipartFile photo) throws IOException {
        Optional<PhotoContact> existingOpt = repo.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        PhotoContact p = existingOpt.get();
        if (name != null) p.setName(name.trim());
        if (relationship != null) p.setRelationship(relationship.trim());
        if (phone != null) p.setPhone(phone.trim());

        if (photo != null && !photo.isEmpty()) {
            // delete previous photo if exists
            if (p.getPhotoFileId() != null) {
                gridFsTemplate.delete(org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(new ObjectId(p.getPhotoFileId()))
                ));
            }
            ObjectId newId = gridFsTemplate.store(photo.getInputStream(), photo.getOriginalFilename(), photo.getContentType());
            p.setPhotoFileId(newId.toHexString());
        }

        PhotoContact saved = repo.save(p);
        return Optional.of(saved);
    }

    public Page<PhotoContact> getContacts(String userId, int page, int size, String q) { // <-- Add userId
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));

        if (q == null || q.trim().isEmpty()) {
            return repo.findByUserId(userId, pageable); // <-- Use a new repository method
        }

        // Prepare a case-insensitive regex for search
        String term = q.trim();
        String regex = "(?i).*" + term + ".*"; // (?i) makes it case-insensitive

        return repo.findByUserIdAndNameRegexOrRelationshipRegexOrPhoneRegex(userId, regex, regex, regex, pageable); // <-- Use a new repository method
    }


    public Optional<GridFsResource> getPhotoResource(String fileId) throws IllegalArgumentException {
        if (fileId == null) return Optional.empty();
        GridFSFile file = gridFsTemplate.findOne(org.springframework.data.mongodb.core.query.Query.query(
                org.springframework.data.mongodb.core.query.Criteria.where("_id").is(new ObjectId(fileId))
        ));
        if (file == null) return Optional.empty();

        GridFsResource resource = gridFsTemplate.getResource(file);
        return Optional.of(resource);
    }

    public Optional<PhotoContact> findById(String id) {
        return repo.findById(id);
    }

    public void deleteContact(String id) {
        repo.findById(id).ifPresent(c -> {
            if (c.getPhotoFileId() != null) {
                gridFsTemplate.delete(org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(new ObjectId(c.getPhotoFileId()))
                ));
            }
            repo.deleteById(id);
        });
    }
}
