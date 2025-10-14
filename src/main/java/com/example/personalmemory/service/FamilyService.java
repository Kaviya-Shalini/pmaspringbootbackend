package com.example.personalmemory.service;

import com.example.personalmemory.model.FamilyConnection;
import com.example.personalmemory.repository.FamilyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FamilyService {

    @Autowired
    private FamilyRepository familyRepository;

    public FamilyConnection createConnection(String patientId, String familyMemberId) {
        // Prevent duplicate connections
        if (familyRepository.existsByPatientIdAndFamilyMemberId(patientId, familyMemberId)) {
            return null; // Or throw an exception for clarity
        }
        FamilyConnection connection = new FamilyConnection(patientId, familyMemberId);
        return familyRepository.save(connection);
    }

    public List<FamilyConnection> getConnectionsForPatient(String patientId) {
        return familyRepository.findByPatientId(patientId);
    }

    public List<FamilyConnection> getConnectionsForFamilyMember(String familyMemberId) {
        return familyRepository.findByFamilyMemberId(familyMemberId);
    }

    public void disconnect(String patientId, String familyMemberId) {
        familyRepository.deleteByPatientIdAndFamilyMemberId(patientId, familyMemberId);
    }

    public List<String> getFamilyMembersByPatientId(String patientId) {
        return familyRepository.findByPatientId(patientId)
                .stream()
                .map(FamilyConnection::getFamilyMemberId)
                .collect(Collectors.toList());
    }
}

