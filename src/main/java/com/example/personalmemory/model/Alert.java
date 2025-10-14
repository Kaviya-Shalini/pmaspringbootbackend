package com.example.personalmemory.model;

import java.time.Instant;

public class Alert {
    private String patientId;
    private double latitude;
    private double longitude;
    private String message;
    private Instant timestamp;
    private String userId; // family member’s userId (receiver)
    private String patientName;

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
}
