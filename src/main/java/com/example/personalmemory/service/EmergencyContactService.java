package com.example.personalmemory.service;

import com.example.personalmemory.model.EmergencyContact;
import com.example.personalmemory.repository.EmergencyContactRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class EmergencyContactService {

    private final EmergencyContactRepository repo;
    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public EmergencyContactService(EmergencyContactRepository repo, GridFsTemplate gridFsTemplate) {
        this.repo = repo;
        this.gridFsTemplate = gridFsTemplate;
    }

    public EmergencyContact addContact(String name, String relationship, String phone, MultipartFile photo) throws IOException {
        if (repo.count() >= 5) {
            throw new IllegalStateException("Maximum 5 emergency contacts allowed.");
        }

        EmergencyContact c = new EmergencyContact();
        c.setName(name.trim());
        c.setRelationship(relationship.trim());
        c.setPhone(phone.trim());
        c.setCreatedAt(Instant.now());

        if (photo != null && !photo.isEmpty()) {
            ObjectId fileId = gridFsTemplate.store(photo.getInputStream(), photo.getOriginalFilename(), photo.getContentType());
            c.setPhotoFileId(fileId.toHexString());
        }

        return repo.save(c);
    }

    public Optional<EmergencyContact> updateContact(String id, String name, String relationship, String phone, MultipartFile photo) throws IOException {
        Optional<EmergencyContact> existingOpt = repo.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        EmergencyContact c = existingOpt.get();
        if (name != null) c.setName(name.trim());
        if (relationship != null) c.setRelationship(relationship.trim());
        if (phone != null) c.setPhone(phone.trim());

        if (photo != null && !photo.isEmpty()) {
            if (c.getPhotoFileId() != null) {
                gridFsTemplate.delete(query(where("_id").is(new ObjectId(c.getPhotoFileId()))));
            }
            ObjectId newId = gridFsTemplate.store(photo.getInputStream(), photo.getOriginalFilename(), photo.getContentType());
            c.setPhotoFileId(newId.toHexString());
        }

        return Optional.of(repo.save(c));
    }

    public Page<EmergencyContact> getContacts(int page, int size, String q) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        if (q == null || q.trim().isEmpty()) return repo.findAll(pageable);

        String regex = "(?i).*" + q.trim() + ".*";
        return repo.findByNameRegexOrRelationshipRegexOrPhoneRegex(regex, regex, regex, pageable);
    }

    public Optional<GridFsResource> getPhotoResource(String fileId) {
        if (fileId == null) return Optional.empty();
        GridFSFile file = gridFsTemplate.findOne(query(where("_id").is(new ObjectId(fileId))));
        if (file == null) return Optional.empty();
        return Optional.of(gridFsTemplate.getResource(file));
    }

    public void deleteContact(String id) {
        repo.findById(id).ifPresent(c -> {
            if (c.getPhotoFileId() != null) {
                gridFsTemplate.delete(query(where("_id").is(new ObjectId(c.getPhotoFileId()))));
            }
            repo.deleteById(id);
        });
    }
}
