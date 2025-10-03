package com.example.personalmemory.service;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.repository.MemoryRepository;
import com.example.personalmemory.exception.ResourceNotFoundException;
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

    private final MemoryRepository memoryRepository;
    private final PatientStatusRepository patientStatusRepository;
    private final Path uploadRoot;

    public MemoryService(MemoryRepository memoryRepository,
                         PatientStatusRepository patientStatusRepository,
                         @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.memoryRepository = memoryRepository;
        this.patientStatusRepository = patientStatusRepository;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public PatientStatus saveOrUpdatePatientStatus(String userId, boolean isAlzheimer) {
        Optional<PatientStatus> opt = patientStatusRepository.findByUserId(userId);
        PatientStatus st;
        if (opt.isPresent()) {
            st = opt.get();
            st.setAlzheimer(isAlzheimer);
            st.setUpdatedAt(new Date());
        } else {
            st = new PatientStatus(userId, isAlzheimer, new Date());
        }
        return patientStatusRepository.save(st);
    }

    public Addmemory saveMemory(Addmemory payload, MultipartFile file, MultipartFile voice) {
        // attach patient status flag if exists
        boolean status = false;
        if (payload.getUserId() != null) {
            Optional<PatientStatus> psOpt = patientStatusRepository.findByUserId(payload.getUserId());
            if (psOpt.isPresent()) {
                status = psOpt.get().isAlzheimer();
            }
        }
        payload.setAlzheimer(status);

        // Save files to disk if present
        if (file != null && !file.isEmpty()) {
            String saved = storeFile(file);
            payload.setFilePath(saved);
        }

        if (voice != null && !voice.isEmpty()) {
            String saved = storeFile(voice);
            payload.setVoicePath(saved);
        }

        payload.setCreatedAt(new Date());
        payload.setUpdatedAt(new Date());

        return memoryRepository.save(payload);
    }

    private String storeFile(MultipartFile multipart) {
        try {
            String original = multipart.getOriginalFilename();
            String filename = System.currentTimeMillis() + "-" + (original != null ? original.replace(" ", "_") : "file");
            Path target = this.uploadRoot.resolve(filename).normalize();
            Files.copy(multipart.getInputStream(), target);
            return target.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public Addmemory getMemory(String id) {
        return memoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Memory not found: " + id));
    }
}
