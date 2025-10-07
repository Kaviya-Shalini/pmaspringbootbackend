package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "family_connections")
public class FamilyConnection {
    @Id
    private String id;

    // owner userId (the account owner whose memories are shared)
    private String userId;

    // username of connected family member (their account username)
    private String familyUsername;

    private Date connectedAt = new Date();

    public FamilyConnection() {}
    public FamilyConnection(String userId, String familyUsername) {
        this.userId = userId;
        this.familyUsername = familyUsername;
        this.connectedAt = new Date();
    }

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFamilyUsername() { return familyUsername; }
    public void setFamilyUsername(String familyUsername) { this.familyUsername = familyUsername; }

    public Date getConnectedAt() { return connectedAt; }
    public void setConnectedAt(Date connectedAt) { this.connectedAt = connectedAt; }
}
