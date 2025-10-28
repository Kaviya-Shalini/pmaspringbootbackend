package com.example.personalmemory.repository;

import com.example.personalmemory.model.RoutineTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalTime;
import java.util.List;

public interface RoutineTaskRepository extends MongoRepository<RoutineTask, String> {
    // Used by the scheduler to find tasks for the current time
    List<RoutineTask> findByScheduledTimeAndRepeatDaily(LocalTime scheduledTime, boolean repeatDaily);

    // Used by the caregiver to view their patient's routines
    List<RoutineTask> findByCaregiverId(String caregiverId);

    // Find tasks for a specific patient
    List<RoutineTask> findByPatientId(String patientId);
}