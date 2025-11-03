// src/main/java/com/example/personalmemory/service/RoutineResponseService.java
package com.example.personalmemory.service;

import com.example.personalmemory.model.RoutineResponse;
import com.example.personalmemory.repository.RoutineResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Comparator;
import java.util.List;

@Service
public class RoutineResponseService {

    @Autowired
    private RoutineResponseRepository responseRepository;




    // Save a new response
    public RoutineResponse recordResponse(String routineId, String patientId, String createdBy, String answer) {
        RoutineResponse response = new RoutineResponse(routineId, patientId, answer, java.time.Instant.now(), createdBy);
        return responseRepository.save(response);
    }

    // Get the most recent response for a given routineId
    public Optional<RoutineResponse> getLatestResponseForRoutine(String routineId) {
        List<RoutineResponse> responses = responseRepository.findByRoutineId(routineId);
        return responses.stream()
                .max(Comparator.comparing(RoutineResponse::getAnsweredAt));
    }

    // ✅ New helper method used by your controller
    public RoutineResponse getLatestByRoutineId(String routineId) {
        return getLatestResponseForRoutine(routineId).orElse(null);
    }
}
