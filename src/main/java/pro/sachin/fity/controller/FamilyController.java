package pro.sachin.fity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.sercives.FamilyService;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/family")
public class FamilyController {
    private final FamilyService familyService;
    
    // TODO: CRITICAL - Don't accept entire Family entity in request body
    // TODO: Create FamilyCreateDTO with familyName and list of memberIds
    // TODO: Auto-generate createdAt timestamp in service layer
    // TODO: Return HttpStatus.CREATED (201) not OK (200)
    @PostMapping("/create")
    ResponseEntity<Family> createFamily(@RequestBody Family family){
        familyService.createFamily(family);
        return ResponseEntity.ok(family);
    }
    
    // TODO: IMPLEMENT - Add member to family
    // TODO: POST /api/v1/family/{familyId}/add-member
    // TODO: Request: memberId, role (primary/spouse/child/member)
    // TODO: Validate member exists and is not already in another family
    // TODO: This requires family_members junction table implementation
    
    // TODO: IMPLEMENT - Remove member from family
    // TODO: DELETE /api/v1/family/{familyId}/member/{memberId}
    // TODO: Check if family still has minimum required members (usually 2)
    // TODO: If primary member removed, need to assign new primary
    
    // TODO: IMPLEMENT - Get family by ID with all members
    // TODO: GET /api/v1/family/{id}
    // TODO: Return family info with list of all members and their roles
    // TODO: Include current subscription status if any
    
    // TODO: IMPLEMENT - Get all members of a family
    // TODO: GET /api/v1/family/{id}/members
    // TODO: Return list of members with their roles (primary, spouse, child)
    
    // TODO: IMPLEMENT - Update family name
    // TODO: PUT /api/v1/family/{id}
    // TODO: Allow changing family name
    
    // TODO: IMPLEMENT - Delete family (soft delete)
    // TODO: DELETE /api/v1/family/{id}
    // TODO: Check if family has active subscriptions
    // TODO: Remove all family_member associations
    
    // TODO: IMPLEMENT - Get family subscription history
    // TODO: GET /api/v1/family/{id}/subscriptions
    // TODO: Return all subscriptions for this family
    
    // TODO: IMPLEMENT - Check family eligibility for plans
    // TODO: GET /api/v1/family/{id}/eligible-plans
    // TODO: Only return FAMILY type plans
    // TODO: Check if number of family members <= plan.maxFamilyMembers
    
    // TODO: VALIDATION - Ensure family has at least 2 members
    // TODO: VALIDATION - Ensure only one primary member per family
    // TODO: VALIDATION - Validate member roles (primary/spouse/child)
    
    // TODO: BUSINESS LOGIC - Family subscription affects all members' access
    // TODO: If family subscription is BLOCKED, all members are blocked
    // TODO: If family subscription is ACTIVE, all members get access
    
    // TODO: MISSING ENTITY - Create FamilyMember junction entity
    // TODO: Fields: familyId, memberId, role (primary/spouse/child/member)
    // TODO: This is critical for family functionality
    
    // TODO: SECURITY - Add authentication and authorization
}
