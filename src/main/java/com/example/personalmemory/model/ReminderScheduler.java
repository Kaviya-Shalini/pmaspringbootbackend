// src/main/java/com/example/personalmemory/service/ReminderScheduler.java
package com.example.personalmemory.model;

import com.example.personalmemory.model.Addmemory;
// The imports for MemoryService and NotificationService are correct as they are in the 'service' package
import com.example.personalmemory.service.MemoryService;
import com.example.personalmemory.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReminderScheduler {

    @Autowired
    private MemoryService memoryService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Checks for reminders that are past their 'reminderAt' time and haven't been delivered yet.
     * Runs every 60 seconds (60000 milliseconds).
     */
    @Scheduled(fixedRate = 10000)
    public void checkAndSendReminders() {
        // 1. Fetch all due and undelivered reminders from the database
        List<Addmemory> dueReminders = memoryService.getDueReminders();

        for (Addmemory memory : dueReminders) {
            // 2. Send WebSocket notification to the specific user.
            // This triggers the real-time update in the user's browser (AppLayoutComponent).
            notificationService.sendReminderNotification(memory);

            // 3. Mark the memory as delivered (reminderDelivered = true)
            // This prevents the same reminder from being sent again in the next 60-second cycle.
            // The logic in markAsRead will handle resetting this for the next day (if daily).
            memory.setReminderDelivered(true);
            memoryService.saveMemory(memory);

            System.out.println("SCHEDULER: Sent and marked as delivered reminder for memory ID: " + memory.getId());
        }
    }
}