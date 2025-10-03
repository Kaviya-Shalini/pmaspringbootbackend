package com.example.personalmemory.service;

import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        String hash = encoder.encode(password);
        User user = new User(username, hash);
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
}
