package com.example.personalmemory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Explicitly list allowed origins for security and compatibility
        String[] allowedOrigins = {"http://localhost:4200", "http://127.0.0.1:4200"};

        registry.addMapping("/api/**") // Apply to all API endpoints
                .allowedOrigins(allowedOrigins)
                // Added PATCH method, used by markAsRead API
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
