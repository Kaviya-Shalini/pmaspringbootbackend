package com.example.personalmemory.repository;

import com.example.personalmemory.model.Routine;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RoutineRepository extends MongoRepository<Routine, String> {
    List<Routine> findByPatientIdAndActiveTrue(String patientId);
    List<Routine> findAllByActiveTrue();
    List<Routine> findByCreatedBy(String createdBy);

}
