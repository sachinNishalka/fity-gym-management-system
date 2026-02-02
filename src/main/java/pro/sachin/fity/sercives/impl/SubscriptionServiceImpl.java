package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionCharges;
import pro.sachin.fity.model.SubscriptionStatus;
import pro.sachin.fity.repository.FamilyRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.repository.PlanRepository;
import pro.sachin.fity.repository.SubscriptionChargesRepository;
import pro.sachin.fity.repository.SubscriptionRepository;
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
                .status(SubscriptionStatus.ACTIVE).build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        createSubscriptionCharges(savedSubscription, subscriptionDTO.getDiscountAmount());

        // here i think we should consider about getting the payment too

        // memberAccessService.updateMemberAccess(subscription.getMember().getId(),
        // graceEndDate, AccessStatus.ALLOWED, "Subscription created, new joinee");

        if (subscription.getMember() != null) {
            memberAccessService.updateMemberAccess(savedSubscription.getMember().getId(), graceEndDate,
                    AccessStatus.ALLOWED, "New subscription created!");
        } else if (subscription.getFamily() != null) {
            // Family subscription - grant access to all family members
            // TODO: Need to implement family member access handling
            // For now, you might skip this or implement basic logic

            Family family = subscription.getFamily();

            for (Member member : family.getMembers()) {
                memberAccessService.updateMemberAccess(member.getId(), graceEndDate, AccessStatus.ALLOWED,
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
        } else if (subscription.getFamily() != null) {
            if (subscriptionRepository.existsByFamilyIdAndStatus(subscription.getFamily().getId(),
                    SubscriptionStatus.ACTIVE)) {
                throw new IllegalStateException("Family already has an active subscription");
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

    // here grace extension by corch
    // manual overwritten of member access by admin

}
