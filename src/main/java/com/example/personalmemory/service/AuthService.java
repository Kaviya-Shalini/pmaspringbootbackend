package com.example.personalmemory.service;

import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.javacpp.indexer.IntIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final String UPLOAD_DIR = "./uploads/faces/";
    private final LBPHFaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
    private final Map<Integer, String> labelToUserIdMap = new HashMap<>();


    @Autowired
    private EncryptionService encryptionService;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        // Load and train the model on startup
        trainFaceRecognizer();
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

    public void registerFace(String userId, MultipartFile faceImage) throws IOException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOpt.get();

        // Create directory if it doesn't exist
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save the image file with a unique name based on the user ID
        String filePath = UPLOAD_DIR + userId + ".jpg";
        Path path = Paths.get(filePath);
        Files.write(path, faceImage.getBytes());

        // Update user with the face image path
        user.setFaceImage(filePath);
        userRepository.save(user);

        // Retrain the recognizer with the new face
        trainFaceRecognizer();
    }

    private void trainFaceRecognizer() {
        List<User> usersWithFaces = userRepository.findAll().stream()
                .filter(user -> user.getFaceImage() != null && !user.getFaceImage().isEmpty())
                .toList();

        if (usersWithFaces.isEmpty()) {
            return; // No faces to train
        }

        MatVector images = new MatVector(usersWithFaces.size());
        Mat labels = new Mat(usersWithFaces.size(), 1, opencv_core.CV_32SC1);
        IntIndexer labelsIndexer = labels.createIndexer();

        labelToUserIdMap.clear(); // Clear the old map
        AtomicInteger labelCounter = new AtomicInteger(0);

        for (User user : usersWithFaces) {
            File imageFile = new File(user.getFaceImage());
            if (!imageFile.exists()) continue;

            Mat img = imread(imageFile.getAbsolutePath(), IMREAD_GRAYSCALE);
            int label = labelCounter.getAndIncrement();

            images.put(label, img);
            labelsIndexer.put(label, 0, label); // Use the simple integer label

            // Map the integer label to the actual MongoDB User ID
            labelToUserIdMap.put(label, user.getId());
        }

        if (images.size() > 0) {
            faceRecognizer.train(images, labels);
        }
    }


    public Optional<User> loginWithFace(MultipartFile faceImage) throws IOException {
        File tempFile = File.createTempFile("face-login", ".jpg");
        faceImage.transferTo(tempFile);
        Mat testImage = imread(tempFile.getAbsolutePath(), IMREAD_GRAYSCALE);
        tempFile.delete();

        if (labelToUserIdMap.isEmpty()) {
            return Optional.empty(); // Can't predict if the model isn't trained
        }

        int[] predictedLabel = new int[1];
        double[] confidence = new double[1];
        faceRecognizer.predict(testImage, predictedLabel, confidence);

        // Confidence threshold: lower is better. You may need to adjust this value.
        if (predictedLabel[0] != -1 && confidence[0] < 80) {
            String userId = labelToUserIdMap.get(predictedLabel[0]);
            if (userId != null) {
                return userRepository.findById(userId);
            }
        }

        return Optional.empty();
    }


    public boolean deleteUser(String userId) {
        if (!userRepository.existsById(userId)) return false;

        // Delete all related collections
        memoryRepository.deleteByUserId(userId);
        emergencyContactRepository.deleteByUserId(userId);
        photoContactRepository.deleteByUserId(userId);
        photoEntryRepository.deleteByOwnerId(userId);
        locationRepository.deleteByUserId(userId);
        // familyRepository.deleteByUserId(userId);
        familyMemberRepository.deleteByUserId(userId);
        alertRepository.deleteByUserId(userId);
        chatRepository.deleteByUserId(userId);
        // Delete user
        userRepository.deleteById(userId);
        return true;
    }
}