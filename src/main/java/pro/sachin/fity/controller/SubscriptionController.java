package pro.sachin.fity.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionStatus;
import pro.sachin.fity.repository.FamilyRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.repository.PlanRepository;
import pro.sachin.fity.sercives.SubscriptionService;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // TODO: CRITICAL - These repositories should NOT be in controller
    // TODO: Move all this logic to SubscriptionService layer
    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final PlanRepository planRepository;

    // TODO: CRITICAL - This endpoint is doing TOO MUCH manual work
    // TODO: The entire method body should be moved to SubscriptionService
    // TODO: Controller should only validate request and call service method
    
    // TODO: CRITICAL - Don't ask client to provide: endDate, dueDate, graceEndDate, status
    // TODO: These should be AUTO-CALCULATED in service layer
    
    // TODO: CRITICAL - This should also create SubscriptionCharges automatically
    // TODO: Use @Transactional to ensure subscription + charges are created together
    
    // TODO: CRITICAL - This should also update MemberAccess if payment is included
    
    // TODO: Rename endpoint to /enroll or /create (more descriptive)
    // TODO: Return HttpStatus.CREATED (201) not OK (200)
    // TODO: Return SubscriptionResponseDTO with complete enrollment details
    @PostMapping("/save")
    ResponseEntity<Subscription> saveSubscription(@RequestBody SubscriptionDTO subscriptionDTO) {

        // TODO: ALL THIS LOGIC BELONGS IN SERVICE LAYER, NOT CONTROLLER
        final Subscription subscription = new Subscription();

        // checking for member id
        if (subscriptionDTO.getMemberId() != null) {
            Member member = memberRepository.findById(subscriptionDTO.getMemberId())
                    .orElseThrow(() -> new EntityNotFoundException("Member is not found"));
            subscription.setMember(member);
        }
        // checking for family id

        if (subscriptionDTO.getFamilyId() != null) {
            Family family = familyRepository.findById(subscriptionDTO.getFamilyId())
                    .orElseThrow(() -> new EntityNotFoundException("Family is not found"));
            subscription.setFamily(family);
        }

        // checking for plan id

        if (subscriptionDTO.getPlanId() != null) {
            Plan plan = planRepository.findById(subscriptionDTO.getPlanId())
                    .orElseThrow(() -> new EntityNotFoundException("Plan is not found"));
            subscription.setPlan(plan);
        }

        // TODO: CLIENT SHOULD NOT PROVIDE THESE DATES - AUTO-CALCULATE THEM
        subscription.setStartDate(subscriptionDTO.getStartDate());
        subscription.setEndDate(subscriptionDTO.getEndDate());
        subscription.setDueDate(subscriptionDTO.getDueDate());
        subscription.setGraceEndDate(subscriptionDTO.getGraceEndDate());
        subscription.setStatus(SubscriptionStatus.valueOf(subscriptionDTO.getStatus()));

        subscriptionService.saveSubscription(subscription);

        return ResponseEntity.ok(subscription);
    }
    
    // TODO: BETTER APPROACH - Create new endpoint: POST /api/v1/subscription/enroll
    // TODO: Request should only contain:
    //       - memberId OR familyId (not both)
    //       - planId
    //       - startDate (optional, default to today)
    //       - discountAmount (optional, default to 0)
    //       - initialPaymentAmount (optional, if paying immediately)
    // TODO: Service layer should:
    //       1. Validate member/family and plan exist
    //       2. Validate no overlapping active subscriptions
    //       3. Calculate all dates automatically
    //       4. Create subscription with status = ACTIVE
    //       5. Create subscription_charges with calculated amounts
    //       6. If initialPayment > 0, create payment record
    //       7. Update member_access based on payment status
    //       8. Schedule renewal reminder notification
    //       9. Return complete enrollment summary
    
    // TODO: IMPLEMENT - Get subscription by ID
    // TODO: GET /api/v1/subscription/{id}
    // TODO: Return subscription with charges, payments, balance due
    
    // TODO: IMPLEMENT - Get active subscription for member
    // TODO: GET /api/v1/subscription/member/{memberId}/active
    // TODO: Return current active subscription or null
    
    // TODO: IMPLEMENT - Get subscription history for member
    // TODO: GET /api/v1/subscription/member/{memberId}/history
    // TODO: Return all past and present subscriptions
    
    // TODO: IMPLEMENT - Renew subscription
    // TODO: POST /api/v1/subscription/{id}/renew
    // TODO: Create new subscription based on old one
    // TODO: Start date = old subscription end date + 1 day
    // TODO: Same plan (or allow plan change)
    
    // TODO: IMPLEMENT - Cancel subscription
    // TODO: POST /api/v1/subscription/{id}/cancel
    // TODO: Set status = ENDED
    // TODO: Update member_access = BLOCKED
    // TODO: Handle prorated refunds if applicable
    
    // TODO: IMPLEMENT - Get subscriptions by status
    // TODO: GET /api/v1/subscription/status/{status}
    // TODO: Return all ACTIVE, IN_GRACE, BLOCKED, or ENDED subscriptions
    // TODO: Use for monitoring dashboard
    
    // TODO: IMPLEMENT - Get subscriptions expiring soon
    // TODO: GET /api/v1/subscription/expiring?days=7
    // TODO: Return subscriptions ending within X days
    // TODO: Use for proactive renewal calls
    
    // TODO: IMPLEMENT - Get subscriptions in grace period
    // TODO: GET /api/v1/subscription/in-grace
    // TODO: SQL: WHERE current_date > end_date AND current_date <= grace_end_date
    // TODO: Use for payment follow-up
    
    // TODO: IMPLEMENT - Get subscriptions with balance due
    // TODO: GET /api/v1/subscription/balance-due
    // TODO: Join with charges and payments to calculate balance
    // TODO: Return subscriptions where payments < net_amount
    
    // TODO: VALIDATION - Add @Valid annotation
    // TODO: ERROR HANDLING - Use @ControllerAdvice for global exception handling
    // TODO: SECURITY - Add authentication, track createdByUserId
}
