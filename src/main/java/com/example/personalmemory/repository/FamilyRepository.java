package com.example.personalmemory.repository;

import com.example.personalmemory.model.FamilyConnection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FamilyRepository extends MongoRepository<FamilyConnection, String> {

    List<FamilyConnection> findByPatientId(String patientId);

    List<FamilyConnection> findByFamilyMemberId(String familyMemberId);

    boolean existsByPatientIdAndFamilyMemberId(String patientId, String familyMemberId);
    void deleteByUserId(String userId);
    @Transactional
    void deleteByPatientIdAndFamilyMemberId(String patientId, String familyMemberId);
    void deleteByTargetUserId(String targetUserId);
}

