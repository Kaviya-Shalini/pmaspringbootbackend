// in kaviya-shalini/pmaspringbootbackend/pmaspringbootbackend-7fe149d3a1ed8327691014420d2b6aba8592c29e/src/main/java/com/example/personalmemory/repository/FamilyRepository.java
package com.example.personalmemory.repository;

import com.example.personalmemory.model.FamilyConnection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyRepository extends MongoRepository<FamilyConnection, String> {
    List<FamilyConnection> findByUserId(String userId);
    // Add this new method
    List<FamilyConnection> findByFamilyUsername(String familyUsername);
    Optional<FamilyConnection> findByUserIdAndFamilyUsername(String userId, String familyUsername);
    void deleteByUserIdAndFamilyUsername(String userId, String familyUsername);
}