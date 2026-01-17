package pro.sachin.fity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import pro.sachin.fity.dto.SubscriptionChargesDTO;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionCharges;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.SubscriptionChargesService;


@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/subscription-charges")
public class SubscriptionChargesController {
    private final SubscriptionChargesService subscriptionChargesService;
    
    // TODO: CRITICAL - Repository should NOT be in controller - move to service
    private final SubscriptionRepository subscriptionRepository;

    // TODO: CRITICAL - This endpoint should probably NOT exist as separate endpoint
    // TODO: Subscription charges should be created AUTOMATICALLY when subscription is created
    // TODO: If you need manual charge adjustment, create separate endpoint: /adjust or /discount
    
    // TODO: CRITICAL - Don't let client provide totalAmount and netAmount
    // TODO: totalAmount should come from subscription.plan.price
    // TODO: netAmount should be calculated: totalAmount - discountAmount
    
    // TODO: CRITICAL - Move all this logic to service layer
    @PostMapping("/save")
    ResponseEntity<SubscriptionCharges> saveSubscriptionCharges(@RequestBody SubscriptionChargesDTO subscriptionChargesDTO) {
        
        // TODO: ALL THIS LOGIC BELONGS IN SERVICE LAYER
        final SubscriptionCharges subscriptionCharges = new SubscriptionCharges();

        if (subscriptionChargesDTO.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.findById(subscriptionChargesDTO.getSubscriptionId())
                    .orElseThrow(() -> new EntityNotFoundException("Subscription is not found"));
            subscriptionCharges.setSubscription(subscription);
        }

        // TODO: Don't accept these from client - calculate automatically
        subscriptionCharges.setTotalAmount(subscriptionChargesDTO.getTotalAmount());
        subscriptionCharges.setDiscountAmount(subscriptionChargesDTO.getDiscountAmount());
        subscriptionCharges.setNetAmount(subscriptionChargesDTO.getNetAmount());
        
        subscriptionChargesService.saveSubscriptionCharges(subscriptionCharges);
        return ResponseEntity.ok(subscriptionCharges);
    }
    
    // TODO: IMPLEMENT - Get charge for subscription
    // TODO: GET /api/v1/subscription-charges/subscription/{subscriptionId}
    // TODO: Return charge details with balance due calculation
    
    // TODO: IMPLEMENT - Apply discount to subscription
    // TODO: POST /api/v1/subscription-charges/{id}/apply-discount
    // TODO: Accept discount amount or percentage
    // TODO: Recalculate netAmount
    // TODO: Log who applied discount (audit trail)
    
    // TODO: IMPLEMENT - Get balance due for subscription
    // TODO: GET /api/v1/subscription-charges/{id}/balance
    // TODO: Calculate: netAmount - SUM(payments.amount)
    // TODO: Return balance due amount
    
    // TODO: IMPLEMENT - Get all subscriptions with outstanding balance
    // TODO: GET /api/v1/subscription-charges/outstanding
    // TODO: Join with payments to find charges where payments < netAmount
    // TODO: Order by balance DESC (highest debt first)
    
    // TODO: VALIDATION - Ensure subscription doesn't already have charges
    // TODO: VALIDATION - Ensure discountAmount <= totalAmount
    // TODO: VALIDATION - Ensure netAmount > 0
    // TODO: SECURITY - Only authorized staff can create/modify charges
}
