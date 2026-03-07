package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import pro.sachin.fity.dto.RenewalRequestDTO;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.Family;
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

    private final PyamentRepository pyamentRepository;

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

        if (currenSubscription.getStatus() != SubscriptionStatus.ACTIVE
                && currenSubscription.getStatus() != SubscriptionStatus.IN_GRACE) {
            throw new IllegalStateException("This subscription cannot be renewed " + currenSubscription.getStatus()
                    + ". Only ACTIVE or IN_GRACE subscriptions can be renewed!"
                    + "Please pay the outstanding balance first");
        }

        // checking if there is a renewal subscription already created

        if (currenSubscription.getMember() != null) {
            if (subscriptionRepository.existsByMemberIdAndStatus(currenSubscription.getMember().getId(),
                    // here i m planning to chnage this to some other state becuas we are using the
                    // same in the membre registration subscription too
                    SubscriptionStatus.PENDING)) {
                throw new IllegalStateException(
                        "A renewal subscription already exist. Please pay for it or cancel the subscription");
            }
        } else if (currenSubscription.getFamily() != null) {
            // here also planning to change the subscription status to something else
            if (subscriptionRepository.existsByFamilyIdAndStatus(currenSubscription.getFamily().getId(),
                    SubscriptionStatus.PENDING)) {
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

        LocalDate startDate = currenSubscription.getEndDate().plusDays(1);
        LocalDate endDate = calculateEndDate(startDate, plan);
        LocalDate dueDate = calculateDueDate(endDate);
        LocalDate graceEndDate = calculateGraceEndDate(endDate);

        // for now its keeping the pending, but have to change for something that can
        // filter out the renewal subscriptions directly
        renewalSubscription = renewalSubscription.toBuilder().startDate(startDate).endDate(endDate).dueDate(dueDate)
                .graceEndDate(graceEndDate)
                .status(SubscriptionStatus.PENDING).build();

        Subscription savedRenewalSubscription = subscriptionRepository.save(renewalSubscription);

        // creating the subscription charges for the renewal subscription

        createSubscriptionCharges(savedRenewalSubscription, renewalRequestDTO.getDiscountAmount());

        return savedRenewalSubscription;

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
