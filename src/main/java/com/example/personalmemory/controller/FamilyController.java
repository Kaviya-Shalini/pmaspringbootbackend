package com.example.personalmemory.controller;

import com.example.personalmemory.model.FamilyMember;
import com.example.personalmemory.service.FamilyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // <-- Import this
import org.springframework.security.core.userdetails.UserDetails; // <-- And this
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @PostMapping
    public FamilyMember addFamilyMember(@RequestBody FamilyMember familyMember, @AuthenticationPrincipal UserDetails currentUser) { // <-- Get the user
        // Pass the username to the service
        return familyService.addFamilyMember(familyMember, currentUser.getUsername());
    }

    @GetMapping
    public List<FamilyMember> getFamilyMembers(@AuthenticationPrincipal UserDetails currentUser) { // <-- Get the user
        return familyService.getFamilyMembers(currentUser.getUsername());
    }

    @DeleteMapping("/{id}")
    public void deleteFamilyMember(@PathVariable String id) {
        familyService.deleteFamilyMember(id);
    }
}