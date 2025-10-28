package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "routineResponses")
public class RoutineResponse {
    @Id
    private String id;
    private String taskId; // Links back to the RoutineTask
    private String patientId;

    private String response; // "YES", "NO", or "MISSED"
    // CRITICAL: Records when the notification was SENT
    private Instant notificationTimestamp;
    // CRITICAL: Records when the patient RESPONDED
    private Instant responseTimestamp;

    // Calculated field: Time in milliseconds to respond
    private long timeToRespondMs;

    // Constructors
    public RoutineResponse() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public Instant getNotificationTimestamp() { return notificationTimestamp; }
    public void setNotificationTimestamp(Instant notificationTimestamp) { this.notificationTimestamp = notificationTimestamp; }
    public Instant getResponseTimestamp() { return responseTimestamp; }
    public void setResponseTimestamp(Instant responseTimestamp) { this.responseTimestamp = responseTimestamp; }
    public long getTimeToRespondMs() { return timeToRespondMs; }
    public void setTimeToRespondMs(long timeToRespondMs) { this.timeToRespondMs = timeToRespondMs; }
}