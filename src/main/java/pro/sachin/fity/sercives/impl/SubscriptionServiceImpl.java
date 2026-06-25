package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import pro.sachin.fity.dto.MemberDTO;
import pro.sachin.fity.dto.MemberDetailsDTO;
import pro.sachin.fity.dto.PlanDTO;
import pro.sachin.fity.dto.RenewalRequestDTO;
import pro.sachin.fity.dto.SubscriptionChargesDTO;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.mapper.MemberMapper;
import pro.sachin.fity.mapper.PlanMapper;
import pro.sachin.fity.mapper.SubscriptionChargesMapper;
import pro.sachin.fity.mapper.SubscriptionMapper;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.GraceExtension;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Payments;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionCharges;
import pro.sachin.fity.model.SubscriptionStatus;
import pro.sachin.fity.repository.*;
import pro.sachin.fity.sercives.MemberAccessService;
import pro.sachin.fity.sercives.SubscriptionService;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    private final PlanRepository planRepository;
    private final SubscriptionChargesRepository subscriptionChargesRepository;
    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final MemberAccessService memberAccessService;
    private final GraceExtensionRepository graceExtensionRepository;

    private final MemberMapper memberMapper;
    private final PlanMapper planMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionChargesMapper subscriptionChargesMapper;

    @Transactional
    @Override
    public void saveSubscription(SubscriptionDTO subscriptionDTO) {

        LocalDate endDate = null;

        Subscription subscription = new Subscription();

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

        subscription.setStartDate(
                subscriptionDTO.getStartDate() != null ? subscriptionDTO.getStartDate() : LocalDate.now());

        checkForOverlappingSubscriptions(subscription);

        if (subscription.getPlan() != null) {
            endDate = calculateEndDate(subscription.getStartDate(), subscription.getPlan());
        }

        LocalDate dueDate = calculateDueDate(endDate);
        LocalDate graceEndDate = calculateGraceEndDate(endDate);
        subscription = subscription.toBuilder().endDate(endDate).dueDate(dueDate).graceEndDate(graceEndDate)
                .status(SubscriptionStatus.PENDING).build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        createSubscriptionCharges(savedSubscription, subscriptionDTO.getDiscountAmount());

        // here i think we should consider about getting the payment too

        // here we are getting the initial payment with subscription

        // for now recoding withe current method, but as i feel this should be changed

        // if (subscriptionDTO.getInitialPaymentAmount() != null
        // && subscriptionDTO.getInitialPaymentAmount().compareTo(BigDecimal.ZERO) > 0)
        // {

        // recordInitialPayment(savedSubscription,
        // subscriptionDTO.getInitialPaymentAmount(), null);
        // }

        // TODO: there is another issue what if the initial payment is a half payment

        // memberAccessService.updateMemberAccess(subscription.getMember().getId(),
        // graceEndDate, AccessStatus.ALLOWED, "Subscription created, new joinee");

        if (subscription.getMember() != null) {
            memberAccessService.updateMemberAccess(savedSubscription.getMember().getId(), graceEndDate,
                    AccessStatus.BLOCKED, "New subscription created!");
        } else if (subscription.getFamily() != null) {
            // Family subscription - grant access to all family members
            // TODO: Need to implement family member access handling
            // For now, you might skip this or implement basic logic

            Family family = subscription.getFamily();

            for (Member member : family.getMembers()) {
                memberAccessService.updateMemberAccess(member.getId(), graceEndDate, AccessStatus.BLOCKED,
                        "Family Subscription " + family.getFamilyName());

                log.info("Granted access to family member {} in family {}", member.getFirstName(),
                        family.getFamilyName());
            }

        }

    }

    private LocalDate calculateEndDate(LocalDate startDate, Plan plan) {
        return startDate.plusDays(plan.getDurationDays());
    }

    private LocalDate calculateDueDate(LocalDate endDate) {
        return endDate.minusDays(7);
    }

    private LocalDate calculateGraceEndDate(LocalDate endDate) {
        return endDate.plusDays(7);
    }

    private void checkForOverlappingSubscriptions(Subscription subscription) {

        if (subscription.getMember() != null) {
            if (subscriptionRepository.existsByMemberIdAndStatus(subscription.getMember().getId(),
                    SubscriptionStatus.ACTIVE)) {
                throw new IllegalStateException("Member already has an active subscription");
            }

            // here checking for overlapping pending subscriptions

            if (subscriptionRepository.existsByMemberIdAndStatus(subscription.getMember().getId(),
                    SubscriptionStatus.PENDING)) {
                throw new IllegalStateException("Member already has a pending susbscription (unpaid)");
            }

        } else if (subscription.getFamily() != null) {
            if (subscriptionRepository.existsByFamilyIdAndStatus(subscription.getFamily().getId(),
                    SubscriptionStatus.ACTIVE)) {
                throw new IllegalStateException("Family already has an active subscription");
            }

            // here checking for pending subscriptions for families
            if (subscriptionRepository.existsByFamilyIdAndStatus(subscription.getFamily().getId(),
                    SubscriptionStatus.PENDING)) {
                throw new IllegalStateException("Family already has a pending subscription (unpaid)");
            }

        } else {
            throw new IllegalStateException("Subscription must have a member or family");
        }

    }

    private void createSubscriptionCharges(Subscription subscription, BigDecimal discountAmount) {

        SubscriptionCharges subscriptionCharges = new SubscriptionCharges();

        subscriptionCharges.setSubscription(subscription);

        if (subscription.getPlan() != null) {
            subscriptionCharges.setTotalAmount(subscription.getPlan().getPrice());
        }

        if (discountAmount != null) {
            subscriptionCharges.setDiscountAmount(discountAmount);
        } else {
            subscriptionCharges.setDiscountAmount(BigDecimal.ZERO);
        }

        BigDecimal netAmount = subscription.getPlan().getPrice().subtract(subscriptionCharges.getDiscountAmount());

        subscriptionCharges.setNetAmount(netAmount);

        subscriptionChargesRepository.save(subscriptionCharges);
    }

    @Override
    public Subscription createRenewalSubscription(RenewalRequestDTO renewalRequestDTO) {
        // TODO Auto-generated method stub

        // get the current subscription

        Subscription currenSubscription = subscriptionRepository.findById(renewalRequestDTO.getCurrentSubscriptionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cannot find an existing subscription to the given id, please check (Subscription Service impl) "));

        // check that subscription is active or in grace, because then only it can be
        // renewed
        // this now checks for due susbcriptions too

        if (currenSubscription.getStatus() != SubscriptionStatus.ACTIVE
                && currenSubscription.getStatus() != SubscriptionStatus.IN_GRACE
                && currenSubscription.getStatus() != SubscriptionStatus.DUE) {
            throw new IllegalStateException("This subscription cannot be renewed " + currenSubscription.getStatus()
                    + ". Only ACTIVE or IN_GRACE or DUE subscriptions can be renewed!"
                    + "Please pay the outstanding balance first");
        }

        // checking if there is a renewal subscription already created

        // changed the subscription status to renewal subscription
        if (currenSubscription.getMember() != null) {
            if (subscriptionRepository.existsByMemberIdAndStatus(currenSubscription.getMember().getId(),
                    // here i m planning to chnage this to some other state becuas we are using the
                    // same in the membre registration subscription too
                    SubscriptionStatus.PENDING_RENEWAL)) {
                throw new IllegalStateException(
                        "A renewal subscription already exist. Please pay for it or cancel the subscription");
            }
        } else if (currenSubscription.getFamily() != null) {
            // here also planning to change the subscription status to something else
            if (subscriptionRepository.existsByFamilyIdAndStatus(currenSubscription.getFamily().getId(),
                    SubscriptionStatus.PENDING_RENEWAL)) {
                throw new IllegalStateException("A renewal subscription already exist for this family");
            }
        }

        // getting the plan for subscribe
        Plan plan = planRepository.findById(renewalRequestDTO.getPlanId())
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));

        // make new subscription
        Subscription renewalSubscription = new Subscription();

        // adding the member or the family to the newly created subscription

        if (currenSubscription.getMember() != null) {
            renewalSubscription.setMember(currenSubscription.getMember());
        } else if (currenSubscription.getFamily() != null) {
            renewalSubscription.setFamily(currenSubscription.getFamily());
        }

        // setting the plan (this is not the existed plan (could be most of the time) or
        // new plan)
        renewalSubscription.setPlan(plan);

        LocalDate startDate = currenSubscription.getEndDate().plusDays(1); // Always use planned start date
        // long borrowedDays = calculateConsumedDays(currenSubscription);
        // LocalDate endDate = calculateFairRenewalEndDate(startDate, plan, borrowedDays);
        LocalDate endDate = calculateEndDate(startDate, plan);
        LocalDate dueDate = calculateDueDate(endDate);
        LocalDate graceEndDate = calculateGraceEndDate(endDate);

        // for now its keeping the pending, but have to change for something that can
        // filter out the renewal subscriptions directly
        renewalSubscription = renewalSubscription.toBuilder().startDate(startDate).endDate(endDate).dueDate(dueDate)
                .graceEndDate(graceEndDate)
                .status(SubscriptionStatus.PENDING_RENEWAL).build();

        Subscription savedRenewalSubscription = subscriptionRepository.save(renewalSubscription);

        // creating the subscription charges for the renewal subscription

        createSubscriptionCharges(savedRenewalSubscription, renewalRequestDTO.getDiscountAmount());

        return savedRenewalSubscription;
    }

    // Add these methods to SubscriptionServiceImpl class

    private LocalDate calculateFairRenewalStartDate(Subscription currentSubscription, Plan plan) {
        LocalDate plannedStartDate = currentSubscription.getEndDate().plusDays(1);
        LocalDate today = LocalDate.now();

        // If renewal is on time or early, use planned date
        if (!today.isAfter(currentSubscription.getGraceEndDate())) {
            log.info("On-time renewal for subscription {}, using planned start date {}",
                    currentSubscription.getId(), plannedStartDate);
            return plannedStartDate;
        }

        // Calculate consumed days beyond grace period
        long consumedDays = calculateConsumedDays(currentSubscription);

        if (consumedDays > 0) {
            // Adjust start date to account for consumed service
            LocalDate adjustedStartDate = plannedStartDate.plusDays(consumedDays);

            log.info("Late renewal adjustment: Subscription {} consumed {} days. " +
                    "Start date adjusted from {} to {}",
                    currentSubscription.getId(),
                    consumedDays, plannedStartDate, adjustedStartDate);

            return adjustedStartDate;
        }

        return plannedStartDate;
    }

    // Remove the old calculateFairRenewalStartDate method entirely and replace
    // with:

    private LocalDate calculateFairRenewalEndDate(LocalDate startDate, Plan plan, long borrowedDays) {
        // Calculate normal end date based on plan duration
        LocalDate normalEndDate = calculateEndDate(startDate, plan);

        if (borrowedDays == 0) {
            log.info("No borrowed days for renewal, using full plan duration: {} days", plan.getDurationDays());
            return normalEndDate;
        }

        // Subtract borrowed days from the plan duration
        LocalDate adjustedEndDate = normalEndDate.minusDays(borrowedDays);

        // Calculate effective duration after adjustment
        long effectiveDuration = java.time.temporal.ChronoUnit.DAYS.between(startDate, adjustedEndDate);

        // Ensure minimum subscription duration (at least 7 days)
        if (effectiveDuration < 7) {
            throw new IllegalStateException("Borrowed days (" + borrowedDays +
                    ") exceed acceptable limits. Effective duration would be " + effectiveDuration +
                    " days. Member should pay additional charges or choose a longer plan. " +
                    "Consider requiring payment for excess borrowed days.");
        }

        log.info("Fair renewal duration adjustment: " +
                "Plan duration: {} days, Borrowed days: {} days, Effective duration: {} days. " +
                "Start: {}, Adjusted end: {} (Normal end would be: {})",
                plan.getDurationDays(), borrowedDays, effectiveDuration,
                startDate, adjustedEndDate, normalEndDate);

        return adjustedEndDate;
    }

    private long calculateConsumedDays(Subscription currentSubscription) {
        LocalDate subscriptionEndDate = currentSubscription.getEndDate();
        LocalDate today = LocalDate.now();

        // If renewal is created before or on subscription end date, no adjustment
        // needed
        if (!today.isAfter(subscriptionEndDate)) {
            log.info("On-time renewal for subscription {}, no borrowed days", currentSubscription.getId());
            return 0;
        }

        // BUSINESS RULE: All days beyond subscription end date are "borrowed" and must
        // be repaid

        // 1. Grace period days (automatic 7 days borrowed)
        LocalDate graceEndDate = currentSubscription.getGraceEndDate();
        long gracePeriodDays = java.time.temporal.ChronoUnit.DAYS.between(subscriptionEndDate, graceEndDate);

        // 2. Calculate coach extension days (each extension is borrowed time)
        List<GraceExtension> extensions = graceExtensionRepository.findBySubscriptionId(currentSubscription.getId());
        long totalExtensionDays = extensions.stream()
                .mapToLong(extension -> {
                    return java.time.temporal.ChronoUnit.DAYS.between(
                            extension.getOldGraceEndDate(),
                            extension.getNewGraceEndDate());
                })
                .sum();

        // 3. Calculate days beyond all extensions (if any)
        LocalDate finalAllowedDate = graceEndDate.plusDays(totalExtensionDays);
        long beyondExtensionDays = 0;
        if (today.isAfter(finalAllowedDate)) {
            beyondExtensionDays = java.time.temporal.ChronoUnit.DAYS.between(finalAllowedDate, today);
        }

        // TOTAL BORROWED DAYS = Grace + Extensions + Beyond
        long totalBorrowedDays = gracePeriodDays + totalExtensionDays + beyondExtensionDays;

        // Update the log message in calculateConsumedDays method:
        log.info("Subscription {} borrowed service breakdown: " +
                "Grace period: {} days, Coach extensions: {} days, Beyond extensions: {} days. " +
                "Total borrowed days: {} days (will reduce next subscription duration). " +
                "Subscription ended: {}, Grace ended: {}, Final allowed: {}, Today: {}",
                currentSubscription.getId(),
                gracePeriodDays, totalExtensionDays, beyondExtensionDays, totalBorrowedDays,
                subscriptionEndDate, graceEndDate, finalAllowedDate, today);

        return totalBorrowedDays;
    }

    // Enhanced method to track renewal adjustments for reporting and transparency
    public Map<String, Object> getRenewalAdjustmentInfo(Long renewalSubscriptionId) {
        Subscription renewal = subscriptionRepository.findById(renewalSubscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Renewal subscription not found"));

        Map<String, Object> info = new HashMap<>();
        info.put("renewalId", renewalSubscriptionId);
        info.put("memberName", getMemberName(renewal));
        info.put("startDate", renewal.getStartDate());
        info.put("endDate", renewal.getEndDate());
        info.put("planDuration", renewal.getPlan().getDurationDays());
        info.put("planName", renewal.getPlan().getName());

        // Calculate what the original start date would have been
        // This requires finding the previous subscription - simplified approach:
        LocalDate calculatedOriginalStart = renewal.getStartDate(); // This would be the adjusted date
        info.put("adjustedStartDate", calculatedOriginalStart);

        return info;
    }

    // Method to get detailed breakdown of borrowed days for transparency
    public Map<String, Object> getBorrowedDaysBreakdown(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found"));

        LocalDate subscriptionEndDate = subscription.getEndDate();
        LocalDate graceEndDate = subscription.getGraceEndDate();
        LocalDate today = LocalDate.now();

        // Calculate each component
        long gracePeriodDays = java.time.temporal.ChronoUnit.DAYS.between(subscriptionEndDate, graceEndDate);

        List<GraceExtension> extensions = graceExtensionRepository.findBySubscriptionId(subscriptionId);
        long totalExtensionDays = extensions.stream()
                .mapToLong(extension -> java.time.temporal.ChronoUnit.DAYS.between(
                        extension.getOldGraceEndDate(), extension.getNewGraceEndDate()))
                .sum();

        LocalDate finalAllowedDate = graceEndDate.plusDays(totalExtensionDays);
        long beyondExtensionDays = today.isAfter(finalAllowedDate)
                ? java.time.temporal.ChronoUnit.DAYS.between(finalAllowedDate, today)
                : 0;

        // Build detailed breakdown
        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("subscriptionId", subscriptionId);
        breakdown.put("memberName", getMemberName(subscription));
        breakdown.put("subscriptionEndDate", subscriptionEndDate);
        breakdown.put("graceEndDate", graceEndDate);
        breakdown.put("gracePeriodDays", gracePeriodDays);
        breakdown.put("numberOfExtensions", extensions.size());
        breakdown.put("totalExtensionDays", totalExtensionDays);
        breakdown.put("finalAllowedDate", finalAllowedDate);
        breakdown.put("beyondExtensionDays", beyondExtensionDays);
        breakdown.put("totalBorrowedDays", gracePeriodDays + totalExtensionDays + beyondExtensionDays);
        breakdown.put("calculationDate", today);

        // Extension details
        List<Map<String, Object>> extensionDetails = extensions.stream()
                .map(ext -> {
                    Map<String, Object> extMap = new HashMap<>();
                    extMap.put("oldGraceEndDate", ext.getOldGraceEndDate());
                    extMap.put("newGraceEndDate", ext.getNewGraceEndDate());
                    extMap.put("extensionDays", java.time.temporal.ChronoUnit.DAYS.between(
                            ext.getOldGraceEndDate(), ext.getNewGraceEndDate()));
                    extMap.put("reason", ext.getReason());
                    extMap.put("extendedBy", ext.getExtendedByUser().getName());
                    return extMap;
                })
                .collect(Collectors.toList());

        breakdown.put("extensionDetails", extensionDetails);

        return breakdown;
    }

    // Add this method for detailed renewal fairness reporting
    public Map<String, Object> getFairRenewalBreakdown(Long currentSubscriptionId, Long planId) {
        Subscription currentSubscription = subscriptionRepository.findById(currentSubscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Current subscription not found"));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));

        LocalDate startDate = currentSubscription.getEndDate().plusDays(1);
        long borrowedDays = calculateConsumedDays(currentSubscription);
        LocalDate normalEndDate = calculateEndDate(startDate, plan);
        LocalDate adjustedEndDate = startDate.plusDays(plan.getDurationDays() - borrowedDays);

        long effectiveDuration = java.time.temporal.ChronoUnit.DAYS.between(startDate, adjustedEndDate);

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("memberName", getMemberName(currentSubscription));
        breakdown.put("currentSubscriptionId", currentSubscriptionId);
        breakdown.put("planName", plan.getName());
        breakdown.put("standardPlanDuration", plan.getDurationDays());
        breakdown.put("borrowedDays", borrowedDays);
        breakdown.put("effectiveDuration", effectiveDuration);
        breakdown.put("renewalStartDate", startDate);
        breakdown.put("normalEndDate", normalEndDate);
        breakdown.put("adjustedEndDate", adjustedEndDate);
        breakdown.put("planPrice", plan.getPrice());
        breakdown.put("daysReduced", borrowedDays);
        breakdown.put("fairnessStatus", borrowedDays == 0 ? "No adjustment needed"
                : "Duration reduced by " + borrowedDays + " days for fair billing");

        // Calculate value metrics
        BigDecimal dailyRate = plan.getPrice().divide(BigDecimal.valueOf(plan.getDurationDays()),
                4, java.math.RoundingMode.HALF_UP);
        BigDecimal borrowedValue = dailyRate.multiply(BigDecimal.valueOf(borrowedDays));
        BigDecimal effectiveValue = dailyRate.multiply(BigDecimal.valueOf(effectiveDuration));

        breakdown.put("dailyRate", dailyRate);
        breakdown.put("borrowedServiceValue", borrowedValue);
        breakdown.put("renewalServiceValue", effectiveValue);
        breakdown.put("totalValueReceived", borrowedValue.add(effectiveValue));

        return breakdown;
    }

    // Add method to generate customer-friendly explanation
    public String generateRenewalExplanation(Long currentSubscriptionId, Long planId) {
        Map<String, Object> breakdown = getFairRenewalBreakdown(currentSubscriptionId, planId);

        String memberName = (String) breakdown.get("memberName");
        String planName = (String) breakdown.get("planName");
        int standardDuration = (Integer) breakdown.get("standardPlanDuration");
        long borrowedDays = (Long) breakdown.get("borrowedDays");
        long effectiveDuration = (Long) breakdown.get("effectiveDuration");
        LocalDate startDate = (LocalDate) breakdown.get("renewalStartDate");
        LocalDate endDate = (LocalDate) breakdown.get("adjustedEndDate");
        BigDecimal planPrice = (BigDecimal) breakdown.get("planPrice");

        if (borrowedDays == 0) {
            return String.format(
                    "Dear %s,\n\n" +
                            "Your %s renewal:\n" +
                            "• Duration: %d days (full plan duration)\n" +
                            "• Period: %s to %s\n" +
                            "• Price: ₹%s\n\n" +
                            "No adjustments needed as you renewed on time!",
                    memberName, planName, standardDuration, startDate, endDate, planPrice);
        } else {
            return String.format(
                    "Dear %s,\n\n" +
                            "Your %s renewal with fair billing adjustment:\n" +
                            "• Standard duration: %d days\n" +
                            "• Borrowed service used: %d days\n" +
                            "• Your renewal duration: %d days\n" +
                            "• Period: %s to %s\n" +
                            "• Price: ₹%s (same as standard plan)\n\n" +
                            "Explanation: You used %d days of service during grace period and extensions. " +
                            "Your renewal duration is adjusted to %d days so you receive exactly %d days " +
                            "of total value for your payment.\n\n" +
                            "This ensures fair billing while honoring our grace period policy.",
                    memberName, planName, standardDuration, borrowedDays, effectiveDuration,
                    startDate, endDate, planPrice, borrowedDays, effectiveDuration, standardDuration);
        }
    }

    private String getMemberName(Subscription subscription) {
        if (subscription.getMember() != null) {
            return subscription.getMember().getFirstName() + " " + subscription.getMember().getLastName();
        } else if (subscription.getFamily() != null) {
            return subscription.getFamily().getFamilyName() + " (Family)";
        }
        return "Unknown";
    }

    @Override
    public List<Subscription> getPendingSubscriptions() {
        List<Subscription> pendingSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.PENDING);
        return pendingSubscriptions;
    }

    @Override
    public List<Subscription> getAllSubscriptions() {
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();
        return allSubscriptions;
    }

    @Override
    public void deleteSubscription(Long subscriptionId) {
        if (subscriptionId == null) {
            throw new IllegalArgumentException("Subscription id cannot be null");
        }
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + subscriptionId));

        if (subscription == null) {
            throw new IllegalStateException("Subscription is not found with the given id");
        }

        subscriptionRepository.delete(subscription);
    }

    @Override
    public MemberDetailsDTO getSubscriptionByMemberId(Long memberId) {
        Subscription subscription = subscriptionRepository.findByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE);
        if (subscription == null) {
            throw new EntityNotFoundException("Active subscription not found for member id: " + memberId);
        }

        MemberDTO memberDTO = memberMapper.toDto(subscription.getMember());

        Member member = memberMapper.toEntity(memberDTO);

        PlanDTO planDTO = planMapper.toDto(subscription.getPlan());

        Plan plan = planMapper.toEntity(planDTO);

        SubscriptionDTO subscriptionDto = subscriptionMapper.toDto(subscription);

        SubscriptionCharges subscriptionCharges = subscription.getSubscriptionCharges();

        
        SubscriptionChargesDTO subscriptionChargesDto = subscriptionChargesMapper.toDto(subscriptionCharges);

        MemberDetailsDTO memberDetailsDTO = new MemberDetailsDTO();
        memberDetailsDTO.setMember(member);
        memberDetailsDTO.setPlan(plan);
        memberDetailsDTO.setSubscription(subscriptionDto);
        memberDetailsDTO.setSubscriptionCharges(subscriptionChargesDto);

        return memberDetailsDTO;
    }

    // here grace extension by corch
    // manual overwritten of member access by admin

    // private void recordInitialPayment(Subscription subscription, BigDecimal
    // paymentAmount, String receiptNo) {
    // Payments payment = new Payments();
    // payment.setSubscription(subscription);
    // payment.setAmount(paymentAmount);
    // payment.setReceiptNo(receiptNo != null ? receiptNo :
    // generateReceiptNumber());
    // payment.setNote("Initial payment during enrollment");

    // pyamentRepository.save(payment);
    // log.info("Payment recorded paymentId={}, Amount={}, Receipt={}",
    // payment.getId(), payment.getAmount(),
    // payment.getReceiptNo());

    // }

    // // access granting logic after subscription

    // private void grantAccessAfterSubscription(Subscription subscription,
    // LocalDate graceEndDate) {
    // if (subscription.getMember() != null) {
    // memberAccessService.updateMemberAccess(subscription.getMember().getId(),
    // graceEndDate, AccessStatus.ALLOWED,
    // "New subscription created!");
    // log.info("Access granted to the member : {}",
    // subscription.getMember().getFirstName());
    // } else if (subscription.getFamily() != null) {
    // Family family = subscription.getFamily();

    // for (Member member : family.getMembers()) {
    // memberAccessService.updateMemberAccess(member.getId(), graceEndDate,
    // AccessStatus.ALLOWED,
    // "Family subscription" + family.getFamilyName());

    // log.info("Access granted for family {} - family member {}",
    // subscription.getFamily().getFamilyName(),
    // member.getFirstName());
    // }
    // }

    // }

    // // generating receipt numbers automatically
    // private String generateReceiptNumber() {
    // LocalDateTime now = LocalDateTime.now();
    // String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    // // TOOD: temporary receipt number
    // int sequenceNumber = (int) (Math.random() * 99999);
    // return String.format("REC-%s-%05d", datePart, sequenceNumber);
    // }

}
