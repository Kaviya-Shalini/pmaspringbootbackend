package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String passwordHash;
    private boolean isAlzheimer = false;
    private String encryptionKey;
    private boolean quickQuestionAnswered = false;

    private String faceImage;      // file path
    private String faceEmbedding;  // embedding vector as string

    public User() {}

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isAlzheimer() { return isAlzheimer; }
    public void setAlzheimer(boolean alzheimer) { isAlzheimer = alzheimer; }

    public String getEncryptionKey() { return encryptionKey; }
    public void setEncryptionKey(String encryptionKey) { this.encryptionKey = encryptionKey; }

    public boolean isQuickQuestionAnswered() { return quickQuestionAnswered; }
    public void setQuickQuestionAnswered(boolean quickQuestionAnswered) { this.quickQuestionAnswered = quickQuestionAnswered; }

    public String getFaceImage() { return faceImage; }
    public void setFaceImage(String faceImage) { this.faceImage = faceImage; }

    public String getFaceEmbedding() { return faceEmbedding; }
    public void setFaceEmbedding(String faceEmbedding) { this.faceEmbedding = faceEmbedding; }
}
