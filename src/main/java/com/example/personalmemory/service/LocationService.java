package com.example.personalmemory.service;

import com.example.personalmemory.model.Location;
import com.example.personalmemory.repository.LocationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class LocationService {

    private final LocationRepository repo;

    public LocationService(LocationRepository repo) {
        this.repo = repo;
    }

    public Optional<Location> getByPatientId(String patientId) {
        return repo.findByPatientId(patientId);
    }

    public Location saveOrUpdate(String patientId, double lat, double lng, String address) {
        Instant now = Instant.now();
        Location loc = repo.findByPatientId(patientId)
                .orElseGet(() -> new Location(patientId, lat, lng, address, now));

        loc.setLatitude(lat);
        loc.setLongitude(lng);
        loc.setAddress(address);
        loc.setSavedAt(now);
        return repo.save(loc);
    }
}
