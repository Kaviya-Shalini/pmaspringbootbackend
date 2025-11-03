package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;

@Document(collection = "routines")
public class Routine {
    @Id
    private String id;
    private String patientId;        // patient userId
    private String createdBy;        // family member userId
    private String question;         // e.g. "Are you woke up?"
    private String timeOfDay;        // store as "HH:mm" e.g. "09:00"
    private boolean repeatDaily;     // true/false
    private boolean active = true;
    @Transient
    private RoutineResponse latestResponse;

    public RoutineResponse getLatestResponse() {
        return latestResponse;
    }

    public void setLatestResponse(RoutineResponse latestResponse) {
        this.latestResponse = latestResponse;
    }

    public Routine() {}

    // constructors/getters/setters
    public Routine(String patientId, String createdBy, String question, String timeOfDay, boolean repeatDaily) {
        this.patientId = patientId;
        this.createdBy = createdBy;
        this.question = question;
        this.timeOfDay = timeOfDay;
        this.repeatDaily = repeatDaily;
        this.active = true;
    }

    // getters and setters omitted for brevity — add them or use Lombok
    // ...
    public String getId(){return id;}
    public void setId(String id){this.id = id;}
    public String getPatientId(){return patientId;}
    public void setPatientId(String patientId){this.patientId = patientId;}
    public String getCreatedBy(){return createdBy;}
    public void setCreatedBy(String createdBy){this.createdBy = createdBy;}
    public String getQuestion(){return question;}
    public void setQuestion(String question){this.question = question;}
    public String getTimeOfDay(){return timeOfDay;}
    public void setTimeOfDay(String timeOfDay){this.timeOfDay = timeOfDay;}
    public boolean isRepeatDaily(){return repeatDaily;}
    public void setRepeatDaily(boolean repeatDaily){this.repeatDaily = repeatDaily;}
    public boolean isActive(){return active;}
    public void setActive(boolean active){this.active = active;}
}
