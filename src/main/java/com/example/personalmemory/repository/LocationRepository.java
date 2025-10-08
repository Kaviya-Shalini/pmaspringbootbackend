package com.example.personalmemory.repository;

import com.example.personalmemory.model.Location;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends MongoRepository<Location, String> {
    Optional<Location> findByPatientIdAndIsPermanent(String patientId, boolean isPermanent);
    void deleteByUserId(String userId);

    List<Location> findByUserId(String userId);
}