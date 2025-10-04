package com.example.personalmemory.repository;

import com.example.personalmemory.model.Addmemory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoryRepository extends MongoRepository<Addmemory, String> {
    List<Addmemory> findByUserId(String userId);
    List<Addmemory> findByUserIdOrderByCreatedAtDesc(String userId);
}
