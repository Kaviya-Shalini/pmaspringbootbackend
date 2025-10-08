package com.example.personalmemory.controller;

import com.example.personalmemory.model.Location;
import com.example.personalmemory.service.LocationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // <-- Import this
import org.springframework.security.core.userdetails.UserDetails; // <-- And this
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public Location saveLocation(@RequestBody Location location, @AuthenticationPrincipal UserDetails currentUser) { // <-- Get the user
        // Pass the username (or user ID) to the service
        return locationService.saveLocation(location, currentUser.getUsername());
    }

    @GetMapping("/{userId}")
    public List<Location> getLocationsByUserId(@PathVariable String userId) {
        return locationService.getLocationsByUserId(userId);
    }
}