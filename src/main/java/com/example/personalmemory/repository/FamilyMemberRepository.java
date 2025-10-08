package com.example.personalmemory.repository;

import com.example.personalmemory.model.FamilyMember;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FamilyMemberRepository extends MongoRepository<FamilyMember, String> {
    List<FamilyMember> findByPatientId(String patientId);
    void deleteByUserId(String userId);
}
