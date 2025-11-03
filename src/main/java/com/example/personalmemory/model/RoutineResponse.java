package com.example.personalmemory.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "routine_responses")
public class RoutineResponse {
    @Id
    private String id;
    private String routineId;
    private String patientId;
    private String answer; // "yes" or "no"
    private Instant answeredAt; // timestamp of click
    private String createdBy; // family member who created the routine (helpful)


    public RoutineResponse() {}

    public RoutineResponse(String routineId, String patientId, String answer, Instant answeredAt, String createdBy) {
        this.routineId = routineId;
        this.patientId = patientId;
        this.answer = answer;
        this.answeredAt = answeredAt;
        this.createdBy = createdBy;
    }

    // getters & setters
    // ...
    public String getId(){return id;}
    public void setId(String id){this.id = id;}
    public String getRoutineId(){return routineId;}
    public void setRoutineId(String routineId){this.routineId = routineId;}
    public String getPatientId(){return patientId;}
    public void setPatientId(String patientId){this.patientId = patientId;}
    public String getAnswer(){return answer;}
    public void setAnswer(String answer){this.answer = answer;}
    public Instant getAnsweredAt(){return answeredAt;}
    public void setAnsweredAt(Instant answeredAt){this.answeredAt = answeredAt;}
    public String getCreatedBy(){return createdBy;}
    public void setCreatedBy(String createdBy){this.createdBy = createdBy;}
}
