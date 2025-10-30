package com.example.personalmemory.repository;

import com.example.personalmemory.model.RoutineResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RoutineResponseRepository extends MongoRepository<RoutineResponse, String> {
    List<RoutineResponse> findByPatientId(String patientId);
    List<RoutineResponse> findByRoutineId(String routineId);
}
