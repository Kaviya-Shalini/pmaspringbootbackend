package com.example.personalmemory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/network")
public class NetworkController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Long>> ping() {
        return ResponseEntity.ok(Map.of("serverTime", System.currentTimeMillis()));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getNetworkStatus(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        // Identify if user is on WiFi or Mobile based on generic headers (simplified)
        // In a real scenario, this is harder, but for a project, we return the IP info.
        return ResponseEntity.ok(Map.of(
                "ipAddress", clientIp,
                "status", "Active"
        ));
    }
}