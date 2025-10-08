package com.example.personalmemory.service;

import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    @Autowired
    private EncryptionService encryptionService;
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    private UserRepository userrepository;
    @Autowired
    private PhotoEntryRepository photoEntryRepository;
    @Autowired
    private PhotoContactRepository photoContactRepository;
    @Autowired
    private MemoryRepository memoryRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private FamilyRepository familyRepository;
    @Autowired
    private FamilyMemberRepository familyMemberRepository;
    @Autowired
    private EmergencyContactRepository emergencyContactRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private AlertRepository alertRepository;

    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        String hash = encoder.encode(password);
        User user = new User(username, hash);
        try {

            String key = encryptionService.generateKey();
            user.setEncryptionKey(key);

        } catch (Exception e) {
            throw new RuntimeException("Could not generate encryption key for user.");
        }
        return userRepository.save(user);
    }

    public Optional<User> login(String username, String password) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isPresent()) {
            User user = opt.get();
            // Check if password matches
            if (encoder.matches(password, user.getPasswordHash())) {
                return Optional.of(user); // Return the user object
            }
        }
        return Optional.empty(); // Return empty if login fails
    }
    public boolean deleteUser(String userId) {
        if (!userRepository.existsById(userId)) return false;

        // Delete all related collections
        memoryRepository.deleteByUserId(userId);
        emergencyContactRepository.deleteByUserId(userId);
        photoContactRepository.deleteByUserId(userId);
        photoEntryRepository.deleteByOwnerId(userId);
        locationRepository.deleteByUserId(userId);
        familyRepository.deleteByUserId(userId);
        familyMemberRepository.deleteByUserId(userId);
        alertRepository.deleteByUserId(userId);
        chatRepository.deleteByUserId(userId);
        // Delete user
        userRepository.deleteById(userId);
        return true;
    }
}
