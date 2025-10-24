// src/main/java/com/example/personalmemory/service/ReminderScheduler.java (New File)
package com.example.personalmemory.model;

import com.example.personalmemory.model.Addmemory;
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

    // Run every 60 seconds (60000 milliseconds)
    @Scheduled(fixedRate = 60000)
    public void checkAndSendReminders() {
        List<Addmemory> dueReminders = memoryService.getDueReminders();

        for (Addmemory memory : dueReminders) {
            // 1. Send WebSocket notification to the specific user.
            notificationService.sendReminderNotification(memory);

            // 2. Mark the memory as delivered so it is not sent again until the user
            // marks it as read (if it's a one-time reminder) or until the next day (if daily).
            memory.setReminderDelivered(true);
            memoryService.saveMemory(memory);
        }
    }
}