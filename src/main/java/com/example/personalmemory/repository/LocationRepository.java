package com.example.personalmemory.repository;

import com.example.personalmemory.model.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface LocationRepository extends MongoRepository<Location, String> {
    // find the single permanent location for a patient (if present)
    Optional<Location> findByPatientIdAndPermanentTrue(String patientId);

    // helper methods (if you need them elsewhere)
    List<Location> findByUserId(String userId);
    List<Location> findByPatientId(String patientId);

    void deleteByUserId(String userId);
}
