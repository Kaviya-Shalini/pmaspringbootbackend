package com.example.personalmemory.service;

import com.example.personalmemory.model.Routine;
import com.example.personalmemory.repository.RoutineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoutineService {

    @Autowired
    private RoutineRepository routineRepository;

    public Routine createRoutine(Routine routine) {
        return routineRepository.save(routine);
    }
    public List<Routine> getRoutinesByFamilyMember(String familyMemberId) {
        return routineRepository.findByCreatedBy(familyMemberId);
    }

    public List<Routine> getRoutinesForPatient(String patientId) {
        return routineRepository.findByPatientIdAndActiveTrue(patientId);
    }

    public Optional<Routine> findById(String id) {
        return routineRepository.findById(id);
    }

    public void deleteRoutine(String routineId) {
        routineRepository.findById(routineId).ifPresent(r -> {
            r.setActive(false); // ✅ properly deactivate
            routineRepository.save(r);
        });
    }
    // 🧩 Create or Save a routine
    public Routine saveRoutine(Routine routine) {
        // If routine ID exists, this will update the record; else it creates new one
        return routineRepository.save(routine);
    }

    public List<Routine> getAllActiveRoutines() {
        return routineRepository.findAllByActiveTrue();
    }
}
