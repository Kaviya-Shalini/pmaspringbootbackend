package com.example.personalmemory.service;

import com.example.personalmemory.model.Addmemory;
import com.example.personalmemory.model.Alert;
import com.example.personalmemory.model.FamilyMember;
import com.example.personalmemory.repository.FamilyMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final FamilyMemberRepository familyRepo;
    private final SimpMessagingTemplate simp;
    private final RestTemplate rest = new RestTemplate();
    

    // FCM server key if you want to enable FCM push notifications
    @Value("${app.fcm.serverKey:}")
    private String fcmServerKey;

    public NotificationService(FamilyMemberRepository familyRepo, SimpMessagingTemplate simp) {
        this.familyRepo = familyRepo;
        this.simp = simp;
    }

    public void notifyFamilyMembers(String patientId, Alert alert) {
        // 1) Save / fetch family members (notify via FCM if tokens exist)
        List<FamilyMember> members = familyRepo.findByPatientId(patientId);

        // Broadcast to WebSocket topic first (any subscribed UI will get it)
        try {
            simp.convertAndSend("/topic/alerts/" + patientId, alert);
        } catch (Exception ex) {
            // ignore websocket send errors for demo
        }

        // If FCM server key is configured, attempt HTTP v1 legacy send (simple)
        if (fcmServerKey != null && !fcmServerKey.isEmpty() && !members.isEmpty()) {
            String fcmUrl = "https://fcm.googleapis.com/fcm/send";
            members.stream()
                    .map(FamilyMember::getFcmToken)
                    .filter(token -> token != null && !token.isEmpty())
                    .forEach(token -> {
                        try {
                            Map<String, Object> body = Map.of(
                                    "to", token,
                                    "notification", Map.of(
                                            "title", "ALERT: Patient may be lost",
                                            "body", alert.getMessage()
                                    ),
                                    "data", Map.of(
                                            "patientId", alert.getPatientId(),
                                            "lat", alert.getLatitude(),
                                            "lng", alert.getLongitude(),
                                            "timestamp", alert.getTimestamp().toString()
                                    )
                            );
                            var headers = new org.springframework.http.HttpHeaders();
                            headers.set("Authorization", "key=" + fcmServerKey);
                            headers.set("Content-Type", "application/json");
                            var req = new org.springframework.http.HttpEntity<>(body, headers);
                            rest.postForEntity(fcmUrl, req, String.class);
                        } catch (Exception e) {
                            // log/send metrics in real app
                        }
                    });
        }

        // Additional channels (email/SMS) can be wired here.
    }
    public void sendReminderNotification(Addmemory memory) {
        // We will use a unique topic for memory reminders
        String destination = "/topic/reminders/" + memory.getUserId();

        // Create a payload containing all necessary info for the frontend
        Map<String, Object> reminderPayload = Map.of(
                "id", memory.getId(),
                "title", memory.getTitle(),
                "description", memory.getDescription(),
                // Frontend needs to know if a voice note exists to get its URL
                "hasVoiceNote", memory.getVoicePath() != null && !memory.getVoicePath().isEmpty(),
                "reminderAt", memory.getReminderAt(),
                "reminderDaily", memory.isReminderDaily()
        );

        try {
            simp.convertAndSend(destination, reminderPayload);
            System.out.println("Sent reminder for memory " + memory.getId() + " to user " + memory.getUserId());
        } catch (Exception ex) {
            // Handle error (e.g., log it)
        }
    }
}
