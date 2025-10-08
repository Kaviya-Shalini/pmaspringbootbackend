package com.example.personalmemory.controller;

import com.example.personalmemory.model.User;
import com.example.personalmemory.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getUser(@PathVariable String userId) {
        User user = dashboardService.getUserInfo(userId);
        return Map.of(
                "userId", user.getId(), // Return userId as well
                "username", user.getUsername(),
                "isAlzheimer", user.isAlzheimer()
        );
    }

    @GetMapping("/memories/recent/{userId}")
    public List<Map<String, Object>> getRecentMemories(@PathVariable String userId) {
        return dashboardService.getRecentMemories(userId);
    }
}
