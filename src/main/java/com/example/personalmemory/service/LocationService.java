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

    public Location saveLocation(String patientId, Location newLocation) {
        newLocation.setPatientId(patientId);

        // If the incoming location is permanent, find the existing one to update it.
        if (newLocation.isPermanent()) {
            Optional<Location> existingOpt = locationRepository.findByPatientIdAndIsPermanent(patientId, true);

            if (existingOpt.isPresent()) {
                // If a permanent location already exists, update its fields
                Location existing = existingOpt.get();
                existing.setLatitude(newLocation.getLatitude());
                existing.setLongitude(newLocation.getLongitude());
                existing.setAddress(newLocation.getAddress());
                return locationRepository.save(existing);
            }
        }

        // If no permanent location exists, save the new one.
        return locationRepository.save(newLocation);
    }

    public Optional<Location> getPermanentLocation(String patientId) {
        return locationRepository.findByPatientIdAndIsPermanent(patientId, true);
    }
}