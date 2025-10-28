package com.example.personalmemory.service;

import com.example.personalmemory.model.RoutineTask;
import com.example.personalmemory.model.RoutineResponse;
import com.example.personalmemory.repository.RoutineTaskRepository;
import com.example.personalmemory.repository.RoutineResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RoutineSchedulerService {

    @Autowired
    private RoutineTaskRepository routineTaskRepository;

    @Autowired
    private RoutineResponseRepository routineResponseRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Run every minute (0 seconds, every minute, every hour, every day of the month, every month, every day of the week)
    // Checks for routines due *now*
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndSendRoutines() {
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);

        // 1. Find all daily repeating tasks scheduled for this minute
        List<RoutineTask> tasks = routineTaskRepository.findByScheduledTimeAndRepeatDaily(now, true);

        // 2. Process and send notifications
        for (RoutineTask task : tasks) {
            String patientId = task.getPatientId();

            // Create a new RoutineResponse to record the notification event
            RoutineResponse initialResponse = new RoutineResponse();
            initialResponse.setTaskId(task.getId());
            initialResponse.setPatientId(patientId);
            // Record the exact time the notification is sent (Crucial for ACRS response time metric)
            initialResponse.setNotificationTimestamp(Instant.now());
            initialResponse.setResponse("PENDING");

            RoutineResponse savedResponse = routineResponseRepository.save(initialResponse);

            // Send via WebSocket to the patient's topic: /topic/routine/{patientId}
            // Use the savedResponse ID as the unique ID for the patient's prompt
            String destination = "/topic/routine/" + patientId;

            // Prepare the payload for the frontend
            RoutineNotificationPayload payload = new RoutineNotificationPayload(
                    savedResponse.getId(),
                    task.getQuestion(),
                    initialResponse.getNotificationTimestamp().toEpochMilli()
            );

            messagingTemplate.convertAndSend(destination, payload);
            System.out.println("Sent routine notification for task: " + task.getQuestion() + " to patient: " + patientId);
        }
    }

    // DTO for the WebSocket payload
    public static class RoutineNotificationPayload {
        public String responseId;
        public String question;
        public long notificationTimeMs;

        public RoutineNotificationPayload(String responseId, String question, long notificationTimeMs) {
            this.responseId = responseId;
            this.question = question;
            this.notificationTimeMs = notificationTimeMs;
        }
    }
}