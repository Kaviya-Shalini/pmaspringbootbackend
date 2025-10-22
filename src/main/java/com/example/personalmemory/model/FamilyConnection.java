package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "familyConnections")
public class FamilyConnection {
    @Id
    private String id;
    private String patientId; // The ID of the patient user
    private String familyMemberId; // The ID of the family member user
    private String userId;
    private String targetId; // <--- This MUST exist in the model



    public FamilyConnection(String patientId, String familyMemberId) {
        this.patientId = patientId;
        this.familyMemberId = familyMemberId;
    }
    // Getters/Setters for targetId
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    // --- Getters and Setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getFamilyMemberId() {
        return familyMemberId;
    }

    public void setFamilyMemberId(String familyMemberId) {
        this.familyMemberId = familyMemberId;
    }
}

