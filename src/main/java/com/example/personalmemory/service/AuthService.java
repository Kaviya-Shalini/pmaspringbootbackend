package com.example.personalmemory.service;

import org.json.JSONObject;
import com.example.personalmemory.model.User;
import com.example.personalmemory.repository.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AuthService with robust, high-accuracy face recognition (Facenet embeddings + face crop).
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
            var inputStream = getClass().getResourceAsStream("/haarcascade_frontalface_default.xml");
            if (inputStream == null) throw new RuntimeException("Haar cascade file not found!");

            File tempFile = File.createTempFile("haarcascade", ".xml");
            tempFile.deleteOnExit();
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            this.faceDetector = new CascadeClassifier(tempFile.getAbsolutePath());
            if (this.faceDetector.empty()) throw new RuntimeException("Failed to load Haar cascade.");
        } catch (IOException e) {
            throw new RuntimeException("Error loading Haar cascade", e);
        }
    }

    // ---------------- BASIC AUTH ----------------

    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent())
            throw new RuntimeException("Username already exists");

        String hash = encoder.encode(password);
        User user = new User(username, hash);
        try {
            user.setEncryptionKey(encryptionService.generateKey());
        } catch (Exception e) {
            throw new RuntimeException("Could not generate encryption key");
        }
        return userRepository.save(user);
    }

    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> encoder.matches(password, user.getPasswordHash()));
    }

    // ---------------- FACE REGISTRATION ----------------

    public void registerFace(String userId, MultipartFile faceImage) throws IOException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");
        User user = userOpt.get();

        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) directory.mkdirs();

        String filePath = UPLOAD_DIR + userId + ".jpg";
        Path path = Paths.get(filePath);
        Files.write(path, faceImage.getBytes());

        // Detect and crop face
        Mat img = opencv_imgcodecs.imread(filePath);
        Mat face = cropFace(img);
        if (face == null) throw new RuntimeException("No clear face detected during registration.");

        float[] embedding = extractFaceEmbedding(face);
        if (embedding == null) throw new RuntimeException("Failed to compute embedding.");

        user.setFaceImage(filePath);
        user.setFaceEmbedding(Arrays.toString(embedding));
        userRepository.save(user);
    }

    // ---------------- FACE LOGIN ----------------

    public Optional<User> loginWithFace(MultipartFile faceImage) throws IOException {
        File tempFile = File.createTempFile("face-login", ".jpg");
        faceImage.transferTo(tempFile);

        Mat img = opencv_imgcodecs.imread(tempFile.getAbsolutePath());
        tempFile.delete();

        Mat face = cropFace(img);
        if (face == null) return Optional.empty();

        float[] loginEmbedding = extractFaceEmbedding(face);
        if (loginEmbedding == null) return Optional.empty();

        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getFaceEmbedding() != null)
                .collect(Collectors.toList());

        double bestScore = 0;
        User bestUser = null;

        for (User user : users) {
            float[] storedEmbedding = parseEmbedding(user.getFaceEmbedding());
            double similarity = cosineSimilarity(loginEmbedding, storedEmbedding);
            if (similarity > bestScore) {
                bestScore = similarity;
                bestUser = user;
            }
        }

        // ✅ Lowered threshold to 0.75 for better tolerance to lighting/dress/background
        if (bestUser != null && bestScore >= 0.75) {
            return Optional.of(bestUser);
        }
        return Optional.empty();
    }

    // ---------------- FACE UTILS ----------------

    private Mat cropFace(Mat img) {
        if (img == null || img.empty()) return null;
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(img, faces);

        if (faces.size() == 0) return null;

        Rect faceRect = faces.get(0);
        Mat face = new Mat(img, faceRect).clone();

        // Resize to 160x160 for Facenet input
        Mat resized = new Mat();
        opencv_imgproc.resize(face, resized, new Size(160, 160));

        return resized;
    }

    private float[] extractFaceEmbedding(Mat faceImg) {
        try {
            File tempFile = File.createTempFile("face-crop", ".jpg");
            opencv_imgcodecs.imwrite(tempFile.getAbsolutePath(), faceImg);

            var url = "http://127.0.0.1:5001/embed";
            var client = java.net.http.HttpClient.newHttpClient();

            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Content-Type", "application/octet-stream")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofFile(tempFile.toPath()))
                    .build();

            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            tempFile.delete();

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
        if (a == null || b == null || a.length != b.length) return 0;
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
