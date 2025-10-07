package com.example.personalmemory.repository;

import com.example.personalmemory.model.FamilyConnection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyRepository extends MongoRepository<FamilyConnection, String> {
    List<FamilyConnection> findByUserId(String userId);
    Optional<FamilyConnection> findByUserIdAndFamilyUsername(String userId, String familyUsername);
    void deleteByUserIdAndFamilyUsername(String userId, String familyUsername);
}
