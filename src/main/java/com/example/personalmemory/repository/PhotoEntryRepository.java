package com.example.personalmemory.repository;

import com.example.personalmemory.model.PhotoEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PhotoEntryRepository extends MongoRepository<PhotoEntry, String> {
    Page<PhotoEntry> findByCaptionRegexIgnoreCase(String captionRegex, Pageable pageable);
}
