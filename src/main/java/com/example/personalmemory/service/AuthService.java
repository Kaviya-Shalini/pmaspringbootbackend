package com.example.personalmemory.service;
import org.json.JSONObject;
import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_imgproc.resize;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;

/**
 * AuthService with high-accuracy face recognition (FaceNet-style embeddings).
 */
@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private EncryptionService encryptionService;
    @Autowired private PhotoEntryRepository photoEntryRepository;
    @Autowired private PhotoContactRepository photoContactRepository;
    @Autowired private MemoryRepository memoryRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private FamilyRepository familyRepository;
    @Autowired private FamilyMemberRepository familyMemberRepository;
    @Autowired private EmergencyContactRepository emergencyContactRepository;
    @Autowired private ChatRepository chatRepository;
    @Autowired private AlertRepository alertRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final String UPLOAD_DIR = "./uploads/faces/";

    private final CascadeClassifier faceDetector;

    public AuthService() {
        try {
            // ✅ Load from classpath (inside src/main/resources)
            var inputStream = getClass().getResourceAsStream("/haarcascade_frontalface_default.xml");
            if (inputStream == null) {
                throw new RuntimeException("Haar cascade file not found in resources!");
            }

            // ✅ Copy to a temporary file (OpenCV needs a real path)
            File tempFile = File.createTempFile("haarcascade_frontalface_default", ".xml");
            tempFile.deleteOnExit();
            java.nio.file.Files.copy(inputStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // ✅ Load into OpenCV
            this.faceDetector = new CascadeClassifier(tempFile.getAbsolutePath());
            if (this.faceDetector.empty()) {
                throw new RuntimeException("Failed to load Haar cascade from temp file: " + tempFile.getAbsolutePath());
            }

        } catch (IOException e) {
            throw new RuntimeException("Error loading Haar cascade file", e);
        }
    }


    // ---------------- BASIC AUTH ----------------

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
            if (encoder.matches(password, user.getPasswordHash())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    // ---------------- FACE REGISTRATION ----------------

    public void registerFace(String userId, MultipartFile faceImage) throws IOException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOpt.get();

        // Save file
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) directory.mkdirs();

        String filePath = UPLOAD_DIR + userId + ".jpg";
        Path path = Paths.get(filePath);
        Files.write(path, faceImage.getBytes());

        // Detect face and compute embedding
        Mat img = imread(filePath);
        float[] embedding = extractFaceEmbedding(img);
        if (embedding == null) {
            throw new RuntimeException("No clear face detected during registration.");
        }

        // Store embedding as string
        user.setFaceImage(filePath);
        user.setFaceImage(Arrays.toString(embedding)); // store as text
        userRepository.save(user);
    }

    // ---------------- FACE LOGIN ----------------

    public Optional<User> loginWithFace(MultipartFile faceImage) throws IOException {
        File tempFile = File.createTempFile("face-login", ".jpg");
        faceImage.transferTo(tempFile);

        Mat img = imread(tempFile.getAbsolutePath());
        tempFile.delete();

        float[] loginEmbedding = extractFaceEmbedding(img);
        if (loginEmbedding == null) {
            return Optional.empty();
        }

        // Compare with all users having stored embeddings
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getFaceImage() != null)
                .collect(Collectors.toList());

        double bestScore = 0;
        User bestUser = null;

        for (User user : users) {
            float[] storedEmbedding = parseEmbedding(user.getFaceImage());
            double similarity = cosineSimilarity(loginEmbedding, storedEmbedding);
            if (similarity > bestScore) {
                bestScore = similarity;
                bestUser = user;
            }
        }

        // Accept only if similarity is >= 0.90
        if (bestUser != null && bestScore >= 0.90) {
            return Optional.of(bestUser);
        }

        return Optional.empty();
    }

    // ---------------- FACE UTILS ----------------

    private float[] extractFaceEmbedding(Mat img) {
        try {
            // Save temporary image
            File tempFile = File.createTempFile("face-temp", ".jpg");
            opencv_imgcodecs.imwrite(tempFile.getAbsolutePath(), img);

            // Send to Python DeepFace API
            var url = "http://127.0.0.1:5001/embed";
            var client = java.net.http.HttpClient.newHttpClient();

            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Content-Type", "application/octet-stream")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofFile(tempFile.toPath()))
                    .build();

            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            tempFile.delete();

            // Parse returned JSON: {"embedding": [0.123, -0.456, ...]}
            JSONObject json = new JSONObject(response.body());

            var arr = json.getJSONArray("embedding");
            float[] embedding = new float[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                embedding[i] = (float) arr.getDouble(i);
            }
            return embedding;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private float[] parseEmbedding(String embeddingStr) {
        String[] parts = embeddingStr.replaceAll("[\\[\\]]", "").split(",");
        float[] emb = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            emb[i] = Float.parseFloat(parts[i].trim());
        }
        return emb;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // ---------------- DELETE USER ----------------

    public boolean deleteUser(String userId) {
        if (!userRepository.existsById(userId)) return false;

        memoryRepository.deleteByUserId(userId);
        emergencyContactRepository.deleteByUserId(userId);
        photoContactRepository.deleteByUserId(userId);
        photoEntryRepository.deleteByOwnerId(userId);
        locationRepository.deleteByUserId(userId);
        familyMemberRepository.deleteByUserId(userId);
        alertRepository.deleteByUserId(userId);
        chatRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
        return true;
    }
}
