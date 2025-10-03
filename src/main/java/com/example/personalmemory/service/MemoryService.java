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
    @Autowired
    private EncryptionService encryptionService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Save/update patient status
    public User savePatientStatus(String userId, Boolean isAlzheimer) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + userId));

        user.setAlzheimer(isAlzheimer);
        user.setQuickQuestionAnswered(true);
        return userRepository.save(user);
    }

    // Get patient status
    public Boolean getPatientStatus(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + userId));
        return user.isAlzheimer();
    }

    public Addmemory createMemory(Addmemory memory, MultipartFile file, MultipartFile voiceNote) throws Exception {
        User user = userRepository.findById(memory.getUserId()).orElseThrow(() ->
                new ResourceNotFoundException("User not found with id: " + memory.getUserId()));
        memory.setAlzheimer(user.isAlzheimer());
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            byte[] encryptedData = encryptionService.encrypt(file.getBytes(), user.getEncryptionKey());
            String fileName = UUID.randomUUID().toString() + ".encrypted";
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, encryptedData);
            memory.setFilePath(filePath.toString());
        }

        // Handle voice note upload
        if (voiceNote != null && !voiceNote.isEmpty()) {
            byte[] encryptedData = encryptionService.encrypt(voiceNote.getBytes(), user.getEncryptionKey());
            String voiceFileName = UUID.randomUUID().toString() + ".encrypted";
            Path voicePath = uploadPath.resolve(voiceFileName);
            Files.write(voicePath, encryptedData);
            memory.setVoicePath(voicePath.toString());
        }

        memory.setCreatedAt(new Date());
        memory.setUpdatedAt(new Date());

        return memoryRepository.save(memory);
    }
}