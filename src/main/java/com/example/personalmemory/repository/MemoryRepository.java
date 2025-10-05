package com.example.personalmemory.repository;

import com.example.personalmemory.model.Addmemory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

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

}
