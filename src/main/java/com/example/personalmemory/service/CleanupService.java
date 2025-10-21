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
    private PhotoEntryRepository photoEntryRepository; // Used to find database records pointing to files

    @Autowired
    private GridFsTemplate gridFsTemplate; // For deleting from fs.files and fs.chunks

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
        List<PhotoEntry> photoEntries = photoEntryRepository.findByUserId(userId); // Fetch entries by userId

        for (PhotoEntry entry : photoEntries) {
            // A. Delete Files from MongoDB GridFS (fs.files and fs.chunks)
            if (entry.getPhotoFileId() != null) {
                try {
                    // FIX: Delete by GridFS file ObjectId (_id) using PhotoFileId
                    Query fileQuery = Query.query(Criteria.where("_id").is(new ObjectId(entry.getPhotoFileId())));
                    gridFsTemplate.delete(fileQuery);
                    logger.debug("Deleted GridFS file (ID: {}): {}", entry.getPhotoFileId(), entry.getCaption());
                } catch (Exception e) {
                    logger.warn("Failed to delete GridFS file (ID: {}): {}", entry.getPhotoFileId(), e.getMessage());
                }
            }

            // B. Delete Encrypted files from local server storage
            // This handles generic encrypted files and potentially voice notes/documents
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

        // --- 2. DELETE REGISTERED FACE IMAGE (Local Server File) ---
        Path faceFilePath = Paths.get(FACE_UPLOAD_DIR + userId + ".jpg"); // Registered face image path
        try {
            Files.deleteIfExists(faceFilePath);
            logger.info("Deleted registered face image for user: {}", userId);
        } catch (IOException e) {
            logger.warn("Failed to delete registered face image {}: {}", faceFilePath, e.getMessage());
        }

        logger.info("Completed file and GridFS cleanup for user: {}", userId);
    }
}
