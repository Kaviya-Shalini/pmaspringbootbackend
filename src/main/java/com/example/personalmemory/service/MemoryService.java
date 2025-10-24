package com.example.personalmemory.service;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.model.DecryptedFile;
import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.MemoryRepository;
import com.example.personalmemory.exception.ResourceNotFoundException;
import com.example.personalmemory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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
    @Autowired
    private NotificationService notificationService; // Inject to use the new method
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
            memory.setOriginalFileName(file.getOriginalFilename());
        }

        // Handle voice note upload
        if (voiceNote != null && !voiceNote.isEmpty()) {
            byte[] encryptedData = encryptionService.encrypt(voiceNote.getBytes(), user.getEncryptionKey());
            String voiceFileName = UUID.randomUUID().toString() + ".encrypted";
            Path voicePath = uploadPath.resolve(voiceFileName);
            Files.write(voicePath, encryptedData);
            memory.setVoicePath(voicePath.toString());
            memory.setOriginalVoiceName(voiceNote.getOriginalFilename());
        }

        memory.setCreatedAt(new Date());
        memory.setUpdatedAt(new Date());

        return memoryRepository.save(memory);
    }
    // In MemoryService.java
    public DecryptedFile getDecryptedMemoryFile(String memoryId, String type) throws Exception {
        Addmemory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Memory not found with id: " + memoryId));

        User user = userRepository.findById(memory.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for memory"));

        String filePath;
        String originalName;

        if ("voice".equalsIgnoreCase(type)) {
            filePath = memory.getVoicePath();
            originalName = memory.getOriginalVoiceName() != null ? memory.getOriginalVoiceName() : "voice_note.mp3";
        } else {
            filePath = memory.getFilePath();
            originalName = memory.getOriginalFileName() != null ? memory.getOriginalFileName() : "file.bin";
        }

        if (filePath == null || filePath.isEmpty())
            throw new ResourceNotFoundException("File not found for this memory");

        byte[] encryptedData = Files.readAllBytes(Paths.get(filePath));
        byte[] decryptedData = encryptionService.decrypt(encryptedData, user.getEncryptionKey());
        System.out.println("Decrypted " + type + " file: " + originalName + ", size = " + decryptedData.length);

        return new DecryptedFile(decryptedData, originalName);
    }
    public Addmemory createMemory(String userId, String title, String category, String customCategory,
                                  String description, MultipartFile file, MultipartFile voiceNote,
                                  Date reminderAt, boolean reminderDaily) throws Exception {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Addmemory memory = new Addmemory();
        memory.setUserId(userId);
        memory.setTitle(title);
        memory.setCategory(category);
        memory.setCustomCategory(customCategory);
        memory.setDescription(description);
        memory.setReminderAt(reminderAt);
        memory.setReminderDaily(reminderDaily);
        memory.setAlzheimer(user.isAlzheimer());
        memory.setCreatedAt(new Date());
        memory.setUpdatedAt(new Date());

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        // 🔹 Encrypt and save uploaded file
        if (file != null && !file.isEmpty()) {
            byte[] encryptedData = encryptionService.encrypt(file.getBytes(), user.getEncryptionKey());
            String fileName = UUID.randomUUID().toString() + ".encrypted";
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, encryptedData);
            memory.setFilePath(filePath.toString());
            memory.setOriginalFileName(file.getOriginalFilename());
        }

        // 🔹 Encrypt and save voice note
        if (voiceNote != null && !voiceNote.isEmpty()) {
            byte[] encryptedData = encryptionService.encrypt(voiceNote.getBytes(), user.getEncryptionKey());
            String voiceName = UUID.randomUUID().toString() + ".encrypted";
            Path voicePath = uploadPath.resolve(voiceName);
            Files.write(voicePath, encryptedData);
            memory.setVoicePath(voicePath.toString());
            memory.setOriginalVoiceName(voiceNote.getOriginalFilename());
        }

        return memoryRepository.save(memory);
    }

    // ✅ Get all memories with pagination & search
    public Page<Addmemory> getAllMemories(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            return memoryRepository.findAll(pageable);
        } else {
            return memoryRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                    search, search, search, pageable
            );
        }
    }

    // ✅ Delete memory and its associated files
    public void deleteMemory(String memoryId) {
        Addmemory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Memory not found with id: " + memoryId));

        try {
            // Delete encrypted file if exists
            if (memory.getFilePath() != null && !memory.getFilePath().isEmpty()) {
                Path filePath = Paths.get(memory.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    System.out.println("Deleted file: " + filePath);
                }
            }

            // Delete voice note file if exists
            if (memory.getVoicePath() != null && !memory.getVoicePath().isEmpty()) {
                Path voicePath = Paths.get(memory.getVoicePath());
                if (Files.exists(voicePath)) {
                    Files.delete(voicePath);
                    System.out.println("Deleted voice note: " + voicePath);
                }
            }

            // Finally delete the memory document
            memoryRepository.delete(memory);

        } catch (IOException e) {
            throw new RuntimeException("Error deleting memory files: " + e.getMessage());
        }
    }

    public Page<Addmemory> getAllMemoriesByUser(String userId, Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            return memoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        } else {
            return memoryRepository.findByUserIdAndSearchTerm(userId, search, pageable);
        }
    }

    public Addmemory saveMemory(Addmemory memory) {
        memory.setUpdatedAt(new Date());
        return memoryRepository.save(memory);
    }

    // ✅ NEW - Get due reminders
    public List<Addmemory> getDueReminders() {
        return memoryRepository.findDueAndUndeliveredReminders(new Date());
    }

    public Addmemory markReminderAsRead(String memoryId) {
        Addmemory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Memory not found with id: " + memoryId));

        // ✅ Mark as read
        memory.setReminderRead(true);
        memory.setReminderDelivered(true); // also mark delivered so it won't resend

        // ✅ Handle daily reminders
        if (memory.isReminderDaily()) {
            LocalDateTime currentReminderTime = memory.getReminderAt()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            LocalDateTime nextReminderTime = currentReminderTime.plusDays(1);
            Date nextReminder = Date.from(nextReminderTime.atZone(ZoneId.systemDefault()).toInstant());

            memory.setReminderAt(nextReminder);
            memory.setReminderRead(false); // reset for next cycle
            memory.setReminderDelivered(false);
        }

        memory.setUpdatedAt(new Date());
        return memoryRepository.save(memory);
    }

    public List<Addmemory> getDueAndUnreadRemindersByUserId(String userId) {
        // ASSUMPTION: The repository has a method to handle the complex query.
        // This method fetches reminders that are due AND reminderDelivered = false.
        // We modify the existing method's intent slightly to filter by user.
        return memoryRepository.findDueAndUndeliveredRemindersByUserId(userId, new Date());
    }


}