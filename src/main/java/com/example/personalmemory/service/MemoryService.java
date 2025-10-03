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
import java.util.UUID;

@Service
public class MemoryService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MemoryRepository memoryRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Save/update patient status
    public User savePatientStatus(String userId, Boolean isAlzheimer) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + userId));

        user.setAlzheimer(isAlzheimer);
        return userRepository.save(user);
    }

    // Get patient status
    public Boolean getPatientStatus(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + userId));
        return user.isAlzheimer();
    }

    // ** NEW METHOD TO SAVE MEMORY **
    public Addmemory createMemory(Addmemory memory, MultipartFile file, MultipartFile voiceNote) throws IOException {
        // Ensure the upload directory exists
        User user = userRepository.findById(memory.getUserId()).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + memory.getUserId()));
        memory.setAlzheimer(user.isAlzheimer());
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
            memory.setFilePath(uploadPath.resolve(fileName).toString());
        }

        // Handle voice note upload
        if (voiceNote != null && !voiceNote.isEmpty()) {
            String voiceFileName = UUID.randomUUID().toString() + "_" + voiceNote.getOriginalFilename();
            Files.copy(voiceNote.getInputStream(), uploadPath.resolve(voiceFileName));
            memory.setVoicePath(uploadPath.resolve(voiceFileName).toString());
        }

        memory.setCreatedAt(new Date());
        memory.setUpdatedAt(new Date());

        return memoryRepository.save(memory);
    }
}