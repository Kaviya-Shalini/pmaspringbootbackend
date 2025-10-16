package com.example.personalmemory.service;

import com.example.personalmemory.model.Location;
import com.example.personalmemory.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Optional<Location> getPermanentLocationByPatientId(String patientId) {
        return locationRepository.findByPatientIdAndPermanentTrue(patientId);
    }

    /**
     * Checks if the given coordinates are within a safe radius (e.g., 50 meters)
     * of the patient's permanent location.
     */
    public boolean isAtPermanentLocation(String patientId, double lat, double lon) {
        Optional<Location> permanentLocation = getPermanentLocationByPatientId(patientId);

        // If no permanent location is set, the patient is always considered 'unsafe' during an alert.
        if (permanentLocation.isEmpty()) {
            return false;
        }

        Location permanent = permanentLocation.get();

        // Haversine formula to calculate distance in meters between two lat/long points
        final double EARTH_RADIUS = 6371000; // Meters
        final double SAFE_RADIUS_METERS = 50.0; // Define safe zone radius

        double dLat = Math.toRadians(permanent.getLatitude() - lat);
        double dLon = Math.toRadians(permanent.getLongitude() - lon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(permanent.getLatitude()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c; // Distance in meters

        return distance <= SAFE_RADIUS_METERS;
    }

    /**
     * Save or update the permanent location for a patient.
     * - If a permanent location exists for the patient: update it (no duplicate).
     * - If none exists: create a new permanent location record.
     */
    public Location saveOrUpdatePermanent(String patientId, Location payload) {
        Optional<Location> existing = locationRepository.findByPatientIdAndPermanentTrue(patientId);
        Location toSave;
        if (existing.isPresent()) {
            toSave = existing.get();
            // update fields (keep same id)
            toSave.setLatitude(payload.getLatitude());
            toSave.setLongitude(payload.getLongitude());
            toSave.setAddress(payload.getAddress());
            toSave.setPermanent(true); // ensure flag is true
            if (payload.getUserId() != null) toSave.setUserId(payload.getUserId());
        } else {
            toSave = new Location();
            toSave.setPatientId(patientId);
            toSave.setLatitude(payload.getLatitude());
            toSave.setLongitude(payload.getLongitude());
            toSave.setAddress(payload.getAddress());
            toSave.setPermanent(true);
            toSave.setUserId(payload.getUserId());
        }
        return locationRepository.save(toSave);
    }

    // general save for other kinds of location records
    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }
}