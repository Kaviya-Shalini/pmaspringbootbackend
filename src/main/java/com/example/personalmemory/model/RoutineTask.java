package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalTime;
import java.time.Instant;

@Document(collection = "routineTasks")
public class RoutineTask {
    @Id
    private String id;
    // The patient this routine is for
    private String patientId;
    // The caregiver who set the routine
    private String caregiverId;

    private String question;
    // Stores only the time of day (e.g., 09:00:00)
    private LocalTime scheduledTime;
    private boolean repeatDaily;
    private Instant createdAt = Instant.now();

    // Constructors
    public RoutineTask() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getCaregiverId() { return caregiverId; }
    public void setCaregiverId(String caregiverId) { this.caregiverId = caregiverId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public LocalTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public boolean isRepeatDaily() { return repeatDaily; }
    public void setRepeatDaily(boolean repeatDaily) { this.repeatDaily = repeatDaily; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}