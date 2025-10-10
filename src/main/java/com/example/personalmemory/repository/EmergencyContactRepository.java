package com.example.personalmemory.repository;

import com.example.personalmemory.model.EmergencyContact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmergencyContactRepository extends MongoRepository<EmergencyContact, String> {
    Page<EmergencyContact> findByNameRegexOrRelationshipRegexOrPhoneRegex(
            String nameRegex, String relRegex, String phoneRegex, Pageable pageable);
    void deleteByUserId(String userId);

    Page<EmergencyContact> findByUserId(String userId, Pageable pageable);

    Page<EmergencyContact> findByUserIdAndNameContainingIgnoreCase(String userId, String q, Pageable pageable);
}
