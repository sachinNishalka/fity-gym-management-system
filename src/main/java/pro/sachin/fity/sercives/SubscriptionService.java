package pro.sachin.fity.sercives;

import pro.sachin.fity.model.Subscription;

public interface SubscriptionService {
    // TODO: CRITICAL - This method is too simple - needs complete rewrite
    // TODO: This should NOT accept fully built Subscription entity
    // TODO: Should accept SubscriptionEnrollmentDTO with minimal fields:
    //       - memberId OR familyId (not both)
    //       - planId
    //       - startDate (optional, defaults to today)
    //       - discountAmount (optional, defaults to 0)
    //       - initialPaymentAmount (optional)
    // TODO: This method should do ALL the business logic:
    //       1. Validate member/family and plan exist
    //       2. Check no overlapping active subscriptions
    //       3. Validate plan type matches (KIDS age check, FAMILY member count)
    //       4. AUTO-CALCULATE dates (endDate, dueDate, graceEndDate)
    //       5. Set status = ACTIVE
    //       6. Create Subscription
    //       7. Create SubscriptionCharges automatically
    //       8. If initialPayment > 0, create Payment record
    //       9. Update MemberAccess based on payment status
    //       10. Schedule notification reminder
    //       11. Return complete enrollment summary
    // TODO: Use @Transactional to ensure all steps succeed or rollback
    void saveSubscription(Subscription subscription);
    
    // TODO: IMPLEMENT - Enroll member/family in plan (better approach)
    // TODO: SubscriptionEnrollmentResponse enrollInPlan(SubscriptionEnrollmentRequest request);
    // TODO: This should replace the basic saveSubscription above
    
    // TODO: IMPLEMENT - Get subscription by ID with full details
    // TODO: SubscriptionDetailDTO getSubscriptionById(Long subscriptionId);
    // TODO: Include: basic info, charges, payments, balance due, days remaining
    
    // TODO: IMPLEMENT - Get active subscription for member
    // TODO: Subscription getActiveMemberSubscription(Long memberId);
    // TODO: Return subscription where member_id = X AND status = 'ACTIVE'
    // TODO: Should be at most one active subscription per member
    
    // TODO: IMPLEMENT - Get active subscription for family
    // TODO: Subscription getActiveFamilySubscription(Long familyId);
    // TODO: Similar to member but for family
    
    // TODO: IMPLEMENT - Get subscription history for member
    // TODO: List<SubscriptionDTO> getMemberSubscriptionHistory(Long memberId);
    // TODO: Return all subscriptions (active and ended) ordered by startDate DESC
    
    // TODO: IMPLEMENT - Renew subscription
    // TODO: Subscription renewSubscription(Long subscriptionId, Long newPlanId, BigDecimal discountAmount);
    // TODO: Create new subscription based on old one
    // TODO: startDate = oldSubscription.endDate + 1 day (or today if already expired)
    // TODO: Allow changing plan during renewal
    // TODO: Create new charges automatically
    
    // TODO: IMPLEMENT - Cancel subscription
    // TODO: void cancelSubscription(Long subscriptionId, String reason, Long cancelledByUserId);
    // TODO: Set status = ENDED
    // TODO: Update member_access = BLOCKED
    // TODO: Handle prorated refunds if applicable
    // TODO: Create audit trail entry
    
    // TODO: IMPLEMENT - Get subscriptions expiring soon
    // TODO: List<SubscriptionDTO> getExpiringSubscriptions(int daysFromNow);
    // TODO: WHERE end_date BETWEEN current_date AND current_date + X days
    // TODO: For proactive renewal calls
    
    // TODO: IMPLEMENT - Get subscriptions in grace period
    // TODO: List<SubscriptionDTO> getSubscriptionsInGrace();
    // TODO: WHERE current_date > end_date AND current_date <= grace_end_date
    // TODO: For payment follow-up
    
    // TODO: IMPLEMENT - Get subscriptions to be blocked
    // TODO: List<Subscription> getSubscriptionsToBlock();
    // TODO: WHERE current_date > grace_end_date AND status != 'BLOCKED'
    // TODO: For automated daily blocking job
    
    // TODO: IMPLEMENT - Block expired subscriptions
    // TODO: void blockExpiredSubscriptions();
    // TODO: Find subscriptions past grace period
    // TODO: Update status = BLOCKED
    // TODO: Update member_access = BLOCKED
    // TODO: Send notification
    // TODO: Run as scheduled job (daily)
    
    // TODO: IMPLEMENT - Calculate balance due for subscription
    // TODO: BigDecimal calculateBalanceDue(Long subscriptionId);
    // TODO: Query: net_amount - SUM(payments.amount)
    // TODO: Return remaining balance
    
    // TODO: IMPLEMENT - Check if subscription is fully paid
    // TODO: boolean isFullyPaid(Long subscriptionId);
    // TODO: Return true if balance due <= 0
    
    // TODO: IMPLEMENT - Get subscriptions with outstanding balance
    // TODO: List<SubscriptionBalanceDTO> getSubscriptionsWithBalance();
    // TODO: Join charges and payments
    // TODO: WHERE net_amount > SUM(payments.amount)
    // TODO: Order by balance DESC
    // TODO: For collections report
    
    // TODO: IMPLEMENT - Validate no overlapping subscriptions
    // TODO: void validateNoOverlappingSubscription(Long memberId, Long familyId);
    // TODO: Check if member/family already has ACTIVE subscription
    // TODO: Throw exception if overlap found
    
    // TODO: IMPLEMENT - Update subscription status (internal use)
    // TODO: void updateSubscriptionStatus(Long subscriptionId, SubscriptionStatus newStatus);
    // TODO: Used by payment service and scheduled jobs
    
    // TODO: IMPLEMENT - Calculate dates automatically
    // TODO: SubscriptionDates calculateSubscriptionDates(LocalDate startDate, int durationDays);
    // TODO: Return object with endDate, dueDate, graceEndDate
    // TODO: Helper method for date calculations
}

