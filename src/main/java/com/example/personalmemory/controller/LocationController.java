package com.example.personalmemory.controller;

import com.example.personalmemory.model.Location;
import com.example.personalmemory.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*") // adjust for production to trusted origins
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // GET /api/patients/{patientId}/location
    @GetMapping("/{patientId}/location")
    public ResponseEntity<?> getLocation(@PathVariable String patientId) {
        return locationService.getByPatientId(patientId)
                .map(loc -> ResponseEntity.ok(loc))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/patients/{patientId}/location (create or save)
    @PostMapping("/{patientId}/location")
    public ResponseEntity<?> saveLocation(@PathVariable String patientId, @RequestBody Location request) {
        double lat = request.getLatitude();
        double lng = request.getLongitude();
        String address = request.getAddress();
        Location saved = locationService.saveOrUpdate(patientId, lat, lng, address);
        return ResponseEntity.ok(saved);
    }

    // PUT /api/patients/{patientId}/location (update)
    @PutMapping("/{patientId}/location")
    public ResponseEntity<?> updateLocation(@PathVariable String patientId, @RequestBody Location request) {
        double lat = request.getLatitude();
        double lng = request.getLongitude();
        String address = request.getAddress();
        Location saved = locationService.saveOrUpdate(patientId, lat, lng, address);
        return ResponseEntity.ok(saved);
    }
}
