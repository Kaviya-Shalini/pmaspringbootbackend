package com.example.personalmemory.service;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.MemoryRepository;
import com.example.personalmemory.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final MemoryRepository memoryRepository;

    public DashboardService(UserRepository userRepository, MemoryRepository memoryRepository) {
        this.userRepository = userRepository;
        this.memoryRepository = memoryRepository;
    }

    public User getUserInfo(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public List<Map<String, Object>> getRecentMemories(String userId) {
        return memoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(m -> {
                    Map<String, Object> memoryMap = new HashMap<>();
                    memoryMap.put("title", m.getTitle());
                    memoryMap.put("category", m.getCategory());
                    memoryMap.put("customCategory", m.getCustomCategory());
                    memoryMap.put("description", m.getDescription());
                    memoryMap.put("createdAt", m.getCreatedAt()); // Ensure createdAt is included
                    return memoryMap;
                })
                .collect(Collectors.toList());
    }
}