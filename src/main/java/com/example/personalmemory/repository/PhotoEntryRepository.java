package com.example.personalmemory.repository;

import com.example.personalmemory.model.PhotoEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

public interface PhotoEntryRepository extends MongoRepository<PhotoEntry, String> {
    Page<PhotoEntry> findByCaptionRegexIgnoreCase(String captionRegex, Pageable pageable);
    void deleteByUserId(String userId);
    @Query("{ 'ownerId' : ?0 }")
    void deleteByOwnerId(String ownerId);
   // Fetch photos by userId
    Page<PhotoEntry> findByUserId(String userId, Pageable pageable);
}
