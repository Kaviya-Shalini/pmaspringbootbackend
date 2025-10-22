package com.example.personalmemory.service;

import com.example.personalmemory.model.PhotoEntry;
import com.example.personalmemory.repository.PhotoEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.types.ObjectId; // Import ObjectId for GridFS ID handling

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class CleanupService {
    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);

    @Autowired
    private PhotoEntryRepository photoEntryRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    // Assuming local storage structure is:
    // 1. Files uploaded with file path in PhotoEntry (e.g., ./uploads/uuid.encrypted)
    // 2. Registered face images (e.g., ./uploads/faces/{userId}.jpg)

    private final String UPLOAD_DIR = "./uploads/";
    private final String FACE_UPLOAD_DIR = "./uploads/faces/";

    /**
     * Deletes all physical files (GridFS and local server files) associated with the user.
     * @param userId The ID of the user whose files should be deleted.
     */
    public void deleteUserFilesAndGridFS(String userId) {
        logger.info("Starting file and GridFS cleanup for user: {}", userId);

        // --- 1. CLEANUP FILES REFERENCED BY PhotoEntry (GridFS and Local Encrypted Files) ---
        // Assuming PhotoEntry has a field 'userId' that links to the owner.
        List<PhotoEntry> photoEntries = photoEntryRepository.findByUserId(userId);

        for (PhotoEntry entry : photoEntries) {
            // A. Delete Files from MongoDB GridFS (fs.files and fs.chunks)
            // 'getPhotoFileId' is assumed to return the String representation of the GridFS ObjectId
            if (entry.getPhotoFileId() != null && !entry.getPhotoFileId().startsWith(UPLOAD_DIR)) {
                try {
                    Query fileQuery = Query.query(Criteria.where("_id").is(new ObjectId(entry.getPhotoFileId())));
                    gridFsTemplate.delete(fileQuery);
                    logger.debug("Deleted GridFS file (ID: {}).", entry.getPhotoFileId());
                } catch (Exception e) {
                    // Log as warning, but continue deletion process for other files/records
                    logger.warn("Failed to delete GridFS file (ID: {}): {}", entry.getPhotoFileId(), e.getMessage());
                }
            }

            // B. Delete Encrypted files from local server storage (files in ./uploads/)
            if (entry.getPhotoFileId() != null && entry.getPhotoFileId().startsWith(UPLOAD_DIR)) {
                Path filePath = Paths.get(entry.getPhotoFileId());
                try {
                    Files.deleteIfExists(filePath);
                    logger.debug("Deleted local encrypted file: {}", entry.getPhotoFileId());
                } catch (IOException e) {
                    logger.warn("Failed to delete local encrypted file {}: {}", entry.getPhotoFileId(), e.getMessage());
                }
            }
        }

        // --- 2. DELETE REGISTERED FACE IMAGE (Local Server File: ./uploads/faces/) ---
        Path faceFilePath = Paths.get(FACE_UPLOAD_DIR + userId + ".jpg");
        try {
            Files.deleteIfExists(faceFilePath);
            logger.info("Deleted registered face image for user: {}", userId);
        } catch (IOException e) {
            logger.warn("Failed to delete registered face image {}: {}", faceFilePath, e.getMessage());
        }

        logger.info("Completed file and GridFS cleanup for user: {}", userId);
    }
}