package com.example.personalmemory.repository;

import com.example.personalmemory.model.PhotoContact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PhotoContactRepository extends MongoRepository<PhotoContact, String> {
    // simple search across name, relationship, phone (case-insensitive contains)
    Page<PhotoContact> findByNameRegexOrRelationshipRegexOrPhoneRegex(String nameRegex, String relRegex, String phoneRegex, Pageable pageable);
    void deleteByUserId(String userId);
    Page<PhotoContact> findByUserId(String userId, Pageable pageable);

    Page<PhotoContact> findByUserIdAndNameContainingIgnoreCase(String userId, String name, Pageable pageable);

}
