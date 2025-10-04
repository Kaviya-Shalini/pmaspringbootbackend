package com.example.personalmemory.service;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.MemoryRepository;
import com.example.personalmemory.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<Object> getRecentMemories(String userId) {
        return memoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(5)
                .map(m -> new Object() {
                    public String title = m.getTitle();
                    public String category = m.getCategory();
                    public String description = m.getDescription();
                })
                .collect(Collectors.toList());
    }
}
