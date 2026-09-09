package pro.sachin.fity.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pro.sachin.fity.dto.FamilyDTO;
import pro.sachin.fity.dto.FamilyResponseDTO;
import pro.sachin.fity.dto.PlanDTO;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.dto.MemberSummeryDTO;
import pro.sachin.fity.sercives.FamilyService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

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
    public ResponseEntity<FamilyResponseDTO> createFamily(@RequestBody FamilyDTO familyDTO) {
        Long familyId = familyService.createFamily(familyDTO).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(familyService.getFamilyById(familyId));
    }

    // TODO: IMPLEMENT - Add member to family
    // TODO: POST /api/v1/family/{familyId}/add-member
    // TODO: Request: memberId, role (primary/spouse/child/member)
    // TODO: Validate member exists and is not already in another family
    // TODO: This requires family_members junction table implementation

    @PostMapping("/{familyId}/members")
    public ResponseEntity<FamilyResponseDTO> addMemberToFamily(@PathVariable Long familyId,
            @RequestParam("memberId") Long memberId) {

        FamilyResponseDTO updatedFamily = familyService.addMemberToFamily(familyId, memberId);

        return new ResponseEntity<FamilyResponseDTO>(updatedFamily, HttpStatus.OK);

    }

    // Backwards-compatible route used by the existing frontend.
    @PutMapping("addmember/{familyId}")
    public ResponseEntity<FamilyResponseDTO> addMemberToFamilyLegacy(@PathVariable Long familyId,
            @RequestParam("memberId") Long memberId) {
        return ResponseEntity.ok(familyService.addMemberToFamily(familyId, memberId));
    }

    // TODO: IMPLEMENT - Remove member from family
    // TODO: DELETE /api/v1/family/{familyId}/member/{memberId}
    // TODO: Check if family still has minimum required members (usually 2)
    // TODO: If primary member removed, need to assign new primary

    @DeleteMapping("/remove-member/{familyId}/{memberId}")
    public ResponseEntity<Void> removeMemberFromFamily(@PathVariable("familyId") Long familyId,
            @PathVariable("memberId") Long memberId) {
        familyService.removeMemberFromFamily(familyId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @GetMapping
    public ResponseEntity<List<FamilyResponseDTO>> getAllFamilies() {
        return ResponseEntity.ok(familyService.getAllFamilies());
    }

    @GetMapping("/{familyId}/members")
    public ResponseEntity<List<MemberSummeryDTO>> getFamilyMembers(@PathVariable Long familyId) {
        return ResponseEntity.ok(familyService.getFamilyMembers(familyId));
    }

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

    // updating family name
    @PutMapping("/update-family-name/{familyId}")
    public ResponseEntity<FamilyResponseDTO> updateFamilyName(@PathVariable("familyId") Long familyId,
            @RequestParam("familyName") String familyName) {
        FamilyResponseDTO updatedFamily = familyService.updateFamilyName(familyId, familyName);
        return new ResponseEntity<FamilyResponseDTO>(updatedFamily, HttpStatus.OK);
    }

    // TODO: IMPLEMENT - Delete family (soft delete)
    // TODO: DELETE /api/v1/family/{id}
    // TODO: Check if family has active subscriptions
    // TODO: Remove all family_member associations

    // delete family
    @DeleteMapping("/delete-family/{familyId}")
    public ResponseEntity<Void> deleteFamily(@PathVariable("familyId") Long familyId) {
        familyService.deleteFamily(familyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // TODO: IMPLEMENT - Get family subscription history
    // TODO: GET /api/v1/family/{id}/subscriptions
    // TODO: Return all subscriptions for this family

    // get all subscriptions for family

    @GetMapping("/{familyId}/subscriptions")
    public ResponseEntity<List<SubscriptionDTO>> getAllSubscriptions(@PathVariable("familyId") Long familyId) {
        List<SubscriptionDTO> subscriptions = familyService.getAllSubscriptionsForFamily(familyId);
        return ResponseEntity.ok(subscriptions);
    }

    // TODO: IMPLEMENT - Check family eligibility for plans
    // TODO: GET /api/v1/family/{id}/eligible-plans
    // TODO: Only return FAMILY type plans
    // TODO: Check if number of family members <= plan.maxFamilyMembers

    // family eligibility for plans
    @GetMapping("/{familyId}/eligible-plans")
    public ResponseEntity<?> getEligiblePlans(@PathVariable("familyId") Long familyId) {
        List<PlanDTO> eligiblePlans = familyService.getEligiblePlansForFamily(familyId);
        return ResponseEntity.ok(eligiblePlans);
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<FamilyResponseDTO> getFamilyById(@PathVariable("id") Long id) {
        FamilyResponseDTO family = familyService.getFamilyById(id);
        return new ResponseEntity<FamilyResponseDTO>(family, HttpStatus.OK);
    }

    // @GetMapping("/{id}")
    // public Long getMethodName(@PathVariable Long id) {
    // return id;
    // }

}
