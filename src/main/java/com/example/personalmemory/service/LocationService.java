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
