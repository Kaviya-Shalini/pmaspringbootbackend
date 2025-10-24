package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "memories")
public class Addmemory {
    @Id
    private String id;

    private String userId;
    private String title;
    private String category;
    private String customCategory;
    private String description;

    // paths where files are saved on server (uploads/)
    private String filePath;
    private String originalFileName;
    private String voicePath;
    private String originalVoiceName;

    private Date reminderAt;
    private boolean reminderDaily;

    // medication-related
    private String medicationName;
    private String dosage;
    private String storageLocation;

    private boolean isAlzheimer;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;
    private boolean reminderDelivered;
    public Addmemory() {}

    // getters & setters (generated)
    // ... (for brevity include all standard getters/setters)
    // --- full implementation below ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public boolean isReminderDelivered() { return reminderDelivered; }
    public void setReminderDelivered(boolean reminderDelivered) { this.reminderDelivered = reminderDelivered; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCustomCategory() { return customCategory; }
    public void setCustomCategory(String customCategory) { this.customCategory = customCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getVoicePath() { return voicePath; }
    public void setVoicePath(String voicePath) { this.voicePath = voicePath; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getOriginalVoiceName() { return originalVoiceName; }
    public void setOriginalVoiceName(String originalVoiceName) { this.originalVoiceName = originalVoiceName; }

    public Date getReminderAt() { return reminderAt; }
    public void setReminderAt(Date reminderAt) { this.reminderAt = reminderAt; }

    public boolean isReminderDaily() { return reminderDaily; }
    public void setReminderDaily(boolean reminderDaily) { this.reminderDaily = reminderDaily; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public boolean isAlzheimer() { return isAlzheimer; }
    public void setAlzheimer(boolean alzheimer) { isAlzheimer = alzheimer; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
