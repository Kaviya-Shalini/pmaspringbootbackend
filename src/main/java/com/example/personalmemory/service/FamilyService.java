package com.example.personalmemory.service;

import com.example.personalmemory.model.FamilyConnection;
import com.example.personalmemory.repository.FamilyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FamilyService {
    @Autowired
    private FamilyRepository familyRepository;

    public FamilyConnection connect(String userId, String familyUsername) {
        // prevent duplicates
        if (familyRepository.findByUserIdAndFamilyUsername(userId, familyUsername).isPresent()) {
            return null;
        }
        FamilyConnection fc = new FamilyConnection(userId, familyUsername);
        return familyRepository.save(fc);
    }

    public List<FamilyConnection> listForUser(String userId) {
        return familyRepository.findByUserId(userId);
    }
    public List<FamilyConnection> listConnectionsForFamilyMember(String familyUsername) {
        return familyRepository.findByFamilyUsername(familyUsername);
    }
    public void disconnect(String userId, String familyUsername) {
        familyRepository.deleteByUserIdAndFamilyUsername(userId, familyUsername);
    }
}
