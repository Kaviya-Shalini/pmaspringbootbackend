package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "locations")
public class Location {
    @Id
    private String id;

    private String patientId;
    private double latitude;
    private double longitude;
    private String address;

    // use a clear property name for Spring Data ("permanent") and map JSON "isPermanent" to it
    private boolean permanent;

    private String userId;

    public Location() {}


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

    // map JSON property name "isPermanent" <-> this 'permanent' field
    @JsonProperty("isPermanent")
    public boolean isPermanent() { return permanent; }

    @JsonProperty("isPermanent")
    public void setPermanent(boolean permanent) { this.permanent = permanent; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
