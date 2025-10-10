package com.example.personalmemory.controller;

import com.example.personalmemory.model.Location;
import com.example.personalmemory.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients/{patientId}/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // GET  /api/patients/{patientId}/location
    @GetMapping
    public ResponseEntity<Location> getPermanentLocation(@PathVariable String patientId) {
        return locationService
                .getPermanentLocationByPatientId(patientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/patients/{patientId}/location  (create or update — this will update if permanent exists)
    @PostMapping
    public ResponseEntity<Location> createOrUpdatePermanent(@PathVariable String patientId,
                                                            @RequestBody Location payload) {
        Location saved = locationService.saveOrUpdatePermanent(patientId, payload);
        return ResponseEntity.ok(saved);
    }

    // PUT /api/patients/{patientId}/location (explicit update — behaves same as POST for safety)
    @PutMapping
    public ResponseEntity<Location> updatePermanent(@PathVariable String patientId,
                                                    @RequestBody Location payload) {
        Location saved = locationService.saveOrUpdatePermanent(patientId, payload);
        return ResponseEntity.ok(saved);
    }
}
