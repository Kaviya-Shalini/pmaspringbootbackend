package com.example.personalmemory.repository;

import com.example.personalmemory.model.RoutineResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.Instant;
import java.util.List;

public interface RoutineResponseRepository extends MongoRepository<RoutineResponse, String> {
    // Used by the caregiver to view historical data
    List<RoutineResponse> findByTaskIdOrderByResponseTimestampDesc(String taskId);

    // Used for the ACRS innovative feature to get response trend
    List<RoutineResponse> findByPatientIdAndResponseTimestampAfter(String patientId, Instant responseTimestamp);
}