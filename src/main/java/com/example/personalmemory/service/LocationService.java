package com.example.personalmemory.service;

import com.example.personalmemory.model.Location;
import com.example.personalmemory.repository.LocationRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location saveLocation(Location location, String userId) { // <-- Add userId here
        location.setUserId(userId); // <-- Set the userId
        return locationRepository.save(location);
    }

    public List<Location> getLocationsByUserId(String userId) {
        return locationRepository.findByUserId(userId);
    }
}