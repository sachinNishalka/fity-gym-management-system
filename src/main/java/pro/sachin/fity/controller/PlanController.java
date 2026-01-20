package pro.sachin.fity.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pro.sachin.fity.dto.PlanDTO;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.sercives.PlanService;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/plan")
public class PlanController {

    private final PlanService planService;

    // TODO: CRITICAL - Don't accept entire Plan entity in request body
    // TODO: Create PlanCreateDTO with validation rules
    // TODO: Set isActive = true by default in service layer
    // TODO: Return HttpStatus.CREATED (201) not OK (200)
    // TODO: Validate plan type specific fields (ageMin/Max for KIDS, maxMembers for FAMILY)
    @PostMapping("/create")
    ResponseEntity<PlanDTO> createPlan(@RequestBody PlanDTO planDTO){
        planService.createPlan(planDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(planDTO);
    }
    
    // TODO: IMPLEMENT - Get all active plans
    // TODO: GET /api/v1/plan/active
    // TODO: Return only plans where isActive = true
    // TODO: Group by plan type (individual, kids, family)
    
    // TODO: IMPLEMENT - Get plan by ID
    // TODO: GET /api/v1/plan/{id}
    // TODO: Return plan details with subscription count
    
    // TODO: IMPLEMENT - Update plan
    // TODO: PUT /api/v1/plan/{id}
    // TODO: Allow updating price, duration, age ranges, max family members
    // TODO: DON'T allow changing plan type (business rule)
    // TODO: Consider versioning plans instead of updating (keep history)
    
    // TODO: IMPLEMENT - Deactivate plan (soft delete)
    // TODO: DELETE /api/v1/plan/{id}
    // TODO: Set isActive = false instead of actual deletion
    // TODO: Existing subscriptions keep their plan data
    
    // TODO: IMPLEMENT - Get plans by type
    // TODO: GET /api/v1/plan/type/{planType}
    // TODO: Return all INDIVIDUAL, KIDS, or FAMILY plans
    
    // TODO: IMPLEMENT - Get eligible plans for member
    // TODO: GET /api/v1/plan/eligible/{memberId}
    // TODO: Filter based on member age for KIDS plans
    // TODO: Consider member's current subscription status
    
    // TODO: VALIDATION - Add @Valid annotation
    // TODO: VALIDATION - Ensure price > 0
    // TODO: VALIDATION - Ensure durationDays is valid (30, 90, 180)
    // TODO: VALIDATION - For KIDS: ageMin < ageMax and both are set
    // TODO: VALIDATION - For FAMILY: maxFamilyMembers >= 2
    // TODO: SECURITY - Only ADMIN should be able to create/update/delete plans
}
