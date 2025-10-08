package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "locations")
public class Location {
    @Id
    private String id;
    private String patientId;
    private double latitude;
    private double longitude;
    private String address;
    private Instant savedAt;

    public Location() {}

    public Location(String patientId, double latitude, double longitude, String address, Instant savedAt) {
        this.patientId = patientId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.savedAt = savedAt;
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Instant getSavedAt() { return savedAt; }
    public void setSavedAt(Instant savedAt) { this.savedAt = savedAt; }
}
