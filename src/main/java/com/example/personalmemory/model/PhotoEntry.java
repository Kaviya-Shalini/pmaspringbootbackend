package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "photos")
public class PhotoEntry {
    @Id
    private String id;
    private String caption;
    private String photoFileId; // stored in GridFS
    private Instant createdAt;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public String getPhotoFileId() { return photoFileId; }
    public void setPhotoFileId(String photoFileId) { this.photoFileId = photoFileId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
