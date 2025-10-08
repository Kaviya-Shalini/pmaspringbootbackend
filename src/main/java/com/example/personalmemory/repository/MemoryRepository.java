// in src/main/java/com/example/personalmemory/repository/MemoryRepository.java
package com.example.personalmemory.repository;

import com.example.personalmemory.model.Addmemory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;


import java.util.List;

@Repository
public interface MemoryRepository extends MongoRepository<Addmemory, String> {
    List<Addmemory> findByUserId(String userId);
    List<Addmemory> findByUserIdOrderByCreatedAtDesc(String userId);
    Page<Addmemory> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    // ✅ Custom search query (title, description, category)
    Page<Addmemory> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String title, String description, String category, Pageable pageable
    );

    // ✅ NEW - Search by userId and a search term
    @Query("{'userId': ?0, '$or': [{'title': {$regex: ?1, $options: 'i'}}, {'description': {$regex: ?1, $options: 'i'}}, {'category': {$regex: ?1, $options: 'i'}}, {'customCategory': {$regex: ?1, $options: 'i'}}]}")
    Page<Addmemory> findByUserIdAndSearchTerm(String userId, String searchTerm, Pageable pageable);
    Page<Addmemory> findByUserIdAndIsAlzheimerOrderByCreatedAtDesc(String userId, boolean isAlzheimer, Pageable pageable);

    @Query("{'userId': ?0, 'isAlzheimer': ?1, '$or': [{'title': {$regex: ?2, $options: 'i'}}, {'description': {$regex: ?2, $options: 'i'}}, {'category': {$regex: ?2, $options: 'i'}}, {'customCategory': {$regex: ?2, $options: 'i'}}]}")
    Page<Addmemory> findByUserIdAndIsAlzheimerAndSearchTerm(String userId, boolean isAlzheimer, String searchTerm, Pageable pageable);

}
