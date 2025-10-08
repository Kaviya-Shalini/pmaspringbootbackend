package com.example.personalmemory.controller;

import com.example.personalmemory.model.Location;
import com.example.personalmemory.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients/{patientId}/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping
    public ResponseEntity<Location> saveLocation(@PathVariable String patientId, @RequestBody Location location) {
        Location savedLocation = locationService.saveLocation(patientId, location);
        return ResponseEntity.ok(savedLocation);
    }

    @GetMapping
    public ResponseEntity<Location> getPermanentLocation(@PathVariable String patientId) {
        return locationService.getPermanentLocation(patientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}