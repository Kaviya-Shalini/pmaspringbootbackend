package com.example.personalmemory.controller;

import com.example.personalmemory.model.Location;
import com.example.personalmemory.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// NOTE: The request mapping is changed to a more general path to host both
// patient-specific and general location endpoints.
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // Class to represent the body of the safety check request
    public static class SafetyCheckRequest {
        private String patientId;
        private double latitude;
        private double longitude;

        // Getters and Setters for Spring's object mapper
        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }

    /**
     * NEW ENDPOINT: Checks the patient's safety status against the permanent location.
     * Uses the 50-meter radius set in LocationService.
     * Path: /api/locations/safety/check
     * @param request Contains the current patientId, latitude, and longitude.
     * @return boolean: true if safe (within 50m radius), false otherwise.
     */
    @PostMapping("/safety/check")
    public ResponseEntity<Boolean> checkSafetyStatus(@RequestBody SafetyCheckRequest request) {
        // Log the check request for debugging (optional)
        System.out.println("Checking safety status for patient " + request.getPatientId()
                + " at (" + request.getLatitude() + ", " + request.getLongitude() + ")");

        boolean isSafe = locationService.isAtPermanentLocation(
                request.getPatientId(),
                request.getLatitude(),
                request.getLongitude()
        );
        return ResponseEntity.ok(isSafe);
    }

    // === EXISTING PERMANENT LOCATION CRUD (Adjusted Mapping) ===

    // GET /api/locations/patients/{patientId}/permanent
    @GetMapping("/patients/{patientId}/permanent")
    public ResponseEntity<Location> getPermanentLocation(@PathVariable String patientId) {
        return locationService
                .getPermanentLocationByPatientId(patientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/locations/patients/{patientId}/permanent
    @PostMapping("/patients/{patientId}/permanent")
    public ResponseEntity<Location> createOrUpdatePermanent(@PathVariable String patientId,
                                                            @RequestBody Location payload) {
        Location saved = locationService.saveOrUpdatePermanent(patientId, payload);
        return ResponseEntity.ok(saved);
    }

    // PUT /api/locations/patients/{patientId}/permanent
    @PutMapping("/patients/{patientId}/permanent")
    public ResponseEntity<Location> updatePermanent(@PathVariable String patientId,
                                                    @RequestBody Location payload) {
        Location saved = locationService.saveOrUpdatePermanent(patientId, payload);
        return ResponseEntity.ok(saved);
    }
}
