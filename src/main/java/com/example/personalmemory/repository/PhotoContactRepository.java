package com.example.personalmemory.repository;

import com.example.personalmemory.model.PhotoContact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PhotoContactRepository extends MongoRepository<PhotoContact, String> {
    // simple search across name, relationship, phone (case-insensitive contains)
    Page<PhotoContact> findByNameRegexOrRelationshipRegexOrPhoneRegex(String nameRegex, String relRegex, String phoneRegex, Pageable pageable);
}
