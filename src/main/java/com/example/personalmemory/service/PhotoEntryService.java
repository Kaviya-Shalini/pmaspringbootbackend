package com.example.personalmemory.service;

import com.example.personalmemory.model.PhotoEntry;
import com.example.personalmemory.repository.PhotoEntryRepository;
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

@Service
public class PhotoEntryService {

    private final PhotoEntryRepository repo;
    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public PhotoEntryService(PhotoEntryRepository repo, GridFsTemplate gridFsTemplate) {
        this.repo = repo;
        this.gridFsTemplate = gridFsTemplate;
    }

    // Add new photo
    public PhotoEntry addPhoto(String caption, MultipartFile photo) throws IOException {
        PhotoEntry entry = new PhotoEntry();
        entry.setCaption(caption != null ? caption.trim() : "");
        entry.setCreatedAt(Instant.now());

        if (photo != null && !photo.isEmpty()) {
            ObjectId fileId = gridFsTemplate.store(photo.getInputStream(), photo.getOriginalFilename(), photo.getContentType());
            entry.setPhotoFileId(fileId.toHexString());
        }

        return repo.save(entry);
    }

    // Update photo
    public Optional<PhotoEntry> updatePhoto(String id, String caption, MultipartFile photo) throws IOException {
        Optional<PhotoEntry> existingOpt = repo.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        PhotoEntry entry = existingOpt.get();
        if (caption != null) entry.setCaption(caption.trim());

        if (photo != null && !photo.isEmpty()) {
            // Delete previous photo from GridFS
            if (entry.getPhotoFileId() != null) {
                gridFsTemplate.delete(org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(new ObjectId(entry.getPhotoFileId()))
                ));
            }
            ObjectId newId = gridFsTemplate.store(photo.getInputStream(), photo.getOriginalFilename(), photo.getContentType());
            entry.setPhotoFileId(newId.toHexString());
        }

        return Optional.of(repo.save(entry));
    }

    // Delete photo
    public void deletePhoto(String id) {
        repo.findById(id).ifPresent(entry -> {
            if (entry.getPhotoFileId() != null) {
                gridFsTemplate.delete(org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(new ObjectId(entry.getPhotoFileId()))
                ));
            }
            repo.deleteById(id);
        });
    }

    // Fetch photos with pagination & search
    public Page<PhotoEntry> getPhotos(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));

        if (search == null || search.trim().isEmpty()) {
            return repo.findAll(pageable);
        }

        String regex = ".*" + search.trim() + ".*";
        return repo.findByCaptionRegexIgnoreCase(regex, pageable);
    }

    // Get photo GridFS resource by fileId
    public Optional<GridFsResource> getPhotoResource(String fileId) {
        if (fileId == null) return Optional.empty();
        GridFSFile file = gridFsTemplate.findOne(org.springframework.data.mongodb.core.query.Query.query(
                org.springframework.data.mongodb.core.query.Criteria.where("_id").is(new ObjectId(fileId))
        ));
        if (file == null) return Optional.empty();
        return Optional.of(gridFsTemplate.getResource(file));
    }
}
