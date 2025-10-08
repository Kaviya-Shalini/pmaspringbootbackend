package com.example.personalmemory.service;

import com.example.personalmemory.model.Location;
import com.example.personalmemory.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public Location saveLocation(String patientId, Location location) {
        location.setPatientId(patientId);
        // If setting a new permanent location, remove the old one first
        if (location.isPermanent()) {
            locationRepository.findByPatientIdAndIsPermanent(patientId, true).ifPresent(loc -> {
                locationRepository.delete(loc);
            });
        }
        return locationRepository.save(location);
    }

    public Optional<Location> getPermanentLocation(String patientId) {
        return locationRepository.findByPatientIdAndIsPermanent(patientId, true);
    }
}