package com.example.personalmemory.service;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.MemoryRepository;
import com.example.personalmemory.exception.ResourceNotFoundException;
import com.example.personalmemory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

@Service
public class MemoryService {
    @Autowired
    private UserRepository userRepository;

    // Save/update patient status
    public User savePatientStatus(String userId, Boolean isAlzheimer) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("User not found with id: " + userId));

        user.setAlzheimer(isAlzheimer);
        return userRepository.save(user);
    }

    // Get patient status
    public Boolean getPatientStatus(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("User not found with id: " + userId));
        return user.isAlzheimer();

    }
}
