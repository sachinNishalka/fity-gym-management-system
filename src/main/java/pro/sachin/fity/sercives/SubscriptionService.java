package pro.sachin.fity.sercives;

import java.math.BigDecimal;
import java.util.List;

import pro.sachin.fity.dto.MemberDetailsDTO;
import pro.sachin.fity.dto.RenewalRequestDTO;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.model.Subscription;

public interface SubscriptionService {

    // COMPLETED: Accepts DTO, validates entities, checks overlaps, auto-calculates
    // dates, creates charges
    // TODO: Still needed:
    // - Validate plan type matches (KIDS age check, FAMILY member count)
    // - If initialPayment > 0, create Payment record
    // - Update MemberAccess based on payment status
    // - Schedule notification reminder
    // - Return SubscriptionResponseDTO instead of void
    void saveSubscription(SubscriptionDTO subscriptionDTO);

    // TODO: IMPLEMENT - Enroll member/family in plan (better approach)
    // TODO: SubscriptionEnrollmentResponse
    // enrollInPlan(SubscriptionEnrollmentRequest request);
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
    // TODO: Subscription renewSubscription(Long subscriptionId, Long newPlanId,
    // BigDecimal discountAmount);
    // TODO: Create new subscription based on old one
    // TODO: startDate = oldSubscription.endDate + 1 day (or today if already
    // expired)
    // TODO: Allow changing plan during renewal
    // TODO: Create new charges automatically

    // TODO: IMPLEMENT - Cancel subscription
    // TODO: void cancelSubscription(Long subscriptionId, String reason, Long
    // cancelledByUserId);
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
    // TODO: void updateSubscriptionStatus(Long subscriptionId, SubscriptionStatus
    // newStatus);
    // TODO: Used by payment service and scheduled jobs

    // TODO: IMPLEMENT - Calculate dates automatically
    // TODO: SubscriptionDates calculateSubscriptionDates(LocalDate startDate, int
    // durationDays);
    // TODO: Return object with endDate, dueDate, graceEndDate
    // TODO: Helper method for date calculations

    Subscription createRenewalSubscription(RenewalRequestDTO renewalRequestDTO);

    List<Subscription> getPendingSubscriptions();

    List<Subscription> getAllSubscriptions();

    void deleteSubscription(Long subscriptionId);

    MemberDetailsDTO getSubscriptionByMemberId(Long memberId);

}
