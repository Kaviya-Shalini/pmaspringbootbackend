package com.example.personalmemory.service;


import com.example.personalmemory.model.FamilyMember;
import com.example.personalmemory.repository.FamilyMemberRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FamilyService {

    private final FamilyMemberRepository familyMemberRepository;

    public FamilyService(FamilyMemberRepository familyMemberRepository) {
        this.familyMemberRepository = familyMemberRepository;
    }

    public FamilyMember addFamilyMember(FamilyMember familyMember, String userId) { // <-- Add userId
        familyMember.setUserId(userId); // <-- Set the userId
        return familyMemberRepository.save(familyMember);
    }

    public List<FamilyMember> getFamilyMembers(String userId) {
        return familyMemberRepository.findByUserId(userId);
    }

    public void deleteFamilyMember(String id) {
        familyMemberRepository.deleteById(id);
    }
}