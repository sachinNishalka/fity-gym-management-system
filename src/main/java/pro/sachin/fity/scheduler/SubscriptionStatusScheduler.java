package pro.sachin.fity.scheduler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.MemberAccess;
import pro.sachin.fity.model.Payments;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionCharges;
import pro.sachin.fity.model.SubscriptionStatus;
import pro.sachin.fity.repository.PyamentRepository;
import pro.sachin.fity.repository.SubscriptionChargesRepository;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.MemberAccessService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionStatusScheduler {

    private final SubscriptionChargesRepository subscriptionChargesRepository;

    private final PyamentRepository pyamentRepository;

    private final SubscriptionRepository subscriptionRepository;

    private final MemberAccessService memberAccessService;

    // SubscriptionStatusScheduler(PyamentRepository pyamentRepository,
    // SubscriptionChargesRepository subscriptionChargesRepository,
    // MemberAccessService memberAccessService) {
    // this.pyamentRepository = pyamentRepository;
    // this.subscriptionChargesRepository = subscriptionChargesRepository;
    // this.memberAccessService = memberAccessService;
    // }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void updateSubscriptionStatuses() {

        LocalDate today = LocalDate.now();
        log.info("Updating subscription statuses for today: {}", today);

        activatePaidRenewals();

        moveToDueStatus(today);

        moveToGracePeriod(today);

        blockExpiredSubscriptions(today);

    }

    public void moveToGracePeriod(LocalDate today) {

        List<SubscriptionStatus> subsriptionList = new ArrayList<>();

        subsriptionList.add(SubscriptionStatus.ACTIVE);
        subsriptionList.add(SubscriptionStatus.DUE);

        List<Subscription> subscriptions = subscriptionRepository.findByEndDateBeforeAndStatusIn(today,
                subsriptionList);

        if (!subscriptions.isEmpty()) {
            subscriptions.forEach(subscription -> subscription.setStatus(SubscriptionStatus.IN_GRACE));
        }
        subscriptionRepository.saveAll(subscriptions);
        log.info("Moved to grace period {} ", subscriptions.size());
    }

    public void moveToDueStatus(LocalDate today) {
        List<Subscription> subscriptions = subscriptionRepository.findByDueDateBeforeAndStatus(today,
                SubscriptionStatus.ACTIVE);

        if (!subscriptions.isEmpty()) {
            subscriptions.forEach(subscription -> subscription.setStatus(SubscriptionStatus.DUE));
        }
        subscriptionRepository.saveAll(subscriptions);
        log.info("Moved to due status {} ", subscriptions.size());
    }

    // public void blockExpiredSubscriptions(LocalDate today) {
    //     List<Subscription> subscriptions = subscriptionRepository.findByGraceEndDateBeforeAndStatus(today,
    //             SubscriptionStatus.IN_GRACE);

    //     if (!subscriptions.isEmpty()) {
    //         subscriptions.forEach(subscription -> {
    //             subscription.setStatus(SubscriptionStatus.BLOCKED);

    //             if (subscription.getMember() != null) {
    //                 memberAccessService.updateMemberAccess(subscription.getMember().getId(), today,
    //                         AccessStatus.BLOCKED, "Grace period expired");
    //             } else if (subscription.getFamily() != null) {
    //                 // TODO: this family subscription should be blocked

    //                 Family family = subscription.getFamily();

    //                 for (Member member : family.getMembers()) {
    //                     memberAccessService.updateMemberAccess(member.getId(), today, AccessStatus.BLOCKED,
    //                             "Family subscription expired!");
    //                     log.info("Blocked access for {} in family {}", member.getFirstName(), family.getFamilyName());
    //                 }

    //             }
    //         });
    //     }
    //     subscriptionRepository.saveAll(subscriptions);
    //     log.info("Blocked expired subscriptions {} ", subscriptions.size());

    // }

    public void blockExpiredSubscriptions(LocalDate today) {
        List<Subscription> subscriptions = subscriptionRepository.findByGraceEndDateBeforeAndStatus(today,
                SubscriptionStatus.IN_GRACE);
    
        if (!subscriptions.isEmpty()) {
            subscriptions.forEach(subscription -> {
    
                if (subscription.getMember() != null) {


                    List<SubscriptionStatus> coveringStatuses = List.of(
                        SubscriptionStatus.ACTIVE,
                        SubscriptionStatus.DUE,
                        SubscriptionStatus.IN_GRACE);
    
                        boolean hasActiveSubscription = subscriptionRepository
                        .existsByMemberIdAndStatusIn(
                                subscription.getMember().getId(),
                                coveringStatuses);
                
    
                    if (hasActiveSubscription) {
                        // Member already has a valid active subscription.
                        // Just mark this old one as ENDED — do NOT touch device access.
                        subscription.setStatus(SubscriptionStatus.ENDED);
                        log.info("Skipped blocking member {} — has an active subscription. Old sub {} marked ENDED.",
                                subscription.getMember().getId(), subscription.getId());
                    } else {
                        subscription.setStatus(SubscriptionStatus.BLOCKED);
                        memberAccessService.updateMemberAccess(subscription.getMember().getId(), today,
                                AccessStatus.BLOCKED, "Grace period expired");
                    }
    
                } else if (subscription.getFamily() != null) {
    
                    Family family = subscription.getFamily();
                    boolean hasActiveSubscription = subscriptionRepository
                            .existsByFamilyIdAndStatus(family.getId(), SubscriptionStatus.ACTIVE);
    
                    if (hasActiveSubscription) {
                        subscription.setStatus(SubscriptionStatus.ENDED);
                        log.info("Skipped blocking family {} — has an active subscription. Old sub {} marked ENDED.",
                                family.getId(), subscription.getId());
                    } else {
                        subscription.setStatus(SubscriptionStatus.BLOCKED);
                        for (Member member : family.getMembers()) {
                            memberAccessService.updateMemberAccess(member.getId(), today, AccessStatus.BLOCKED,
                                    "Family subscription expired!");
                            log.info("Blocked access for {} in family {}", member.getFirstName(), family.getFamilyName());
                        }
                    }
                }
            });
        }
        subscriptionRepository.saveAll(subscriptions);
        log.info("Blocked expired subscriptions {} ", subscriptions.size());
    }

    public void activatePaidRenewals() {
        LocalDate today = LocalDate.now();

        List<Subscription> pendingRenewals = subscriptionRepository
                .findByStatusAndStartDateBefore(SubscriptionStatus.PENDING_RENEWAL, today.plusDays(1));

        for (Subscription renewal : pendingRenewals) {

            if (isSubscriptionFullyPaid(renewal.getId())) {

                // setting the subscription status to active
                renewal.setStatus(SubscriptionStatus.ACTIVE);

                // NEW: End the old subscription when renewal is activated
                endOldSubscriptionOnRenewal(renewal);

                // granting the access to the members
                if (renewal.getMember() != null) {
                    memberAccessService.updateMemberAccess(renewal.getMember().getId(), renewal.getGraceEndDate(),
                            AccessStatus.ALLOWED,
                            "Renewal Subscription Activated");
                    log.info("Activated renewal subscription {} for member {}", renewal.getId(),
                            renewal.getMember().getId());
                } else if (renewal.getFamily() != null) {
                    Family family = renewal.getFamily();
                    for (Member member : family.getMembers()) {
                        memberAccessService.updateMemberAccess(member.getId(), renewal.getGraceEndDate(),
                                AccessStatus.ALLOWED, "Family renewal activated!");
                    }

                    log.info("Activated renewal subsctiption {} for family {}", renewal.getId(), family.getId());
                }

            }
        }

        subscriptionRepository.saveAll(pendingRenewals);
        log.info("Activated {} renewal subscriptions", pendingRenewals.size());
    }

    // check is the subscription fully paid or not

    private boolean isSubscriptionFullyPaid(Long subscriptionId) {
        List<Payments> payments = pyamentRepository.findBySubscriptionId(subscriptionId);

        // getting total payed in the payments

        // study the calculation function for total payment in isSubscriptionFullyPaid
        // method
        BigDecimal totalPaid = payments.stream().map(Payments::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // finding subscription charges for the subscription

        SubscriptionCharges charges = subscriptionChargesRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException("Charges not found"));

        return totalPaid.compareTo(charges.getNetAmount()) >= 0;
    }

    // Add this method to SubscriptionServiceImpl class
    private void endOldSubscriptionOnRenewal(Subscription renewalSubscription) {
        if (renewalSubscription.getMember() != null) {
            // Find the old active subscription for this member
            Optional<Subscription> oldSubscription = subscriptionRepository
                    .findByMemberIdAndStatusIn(renewalSubscription.getMember().getId(),
                            Arrays.asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.DUE,
                                    SubscriptionStatus.IN_GRACE));

            if (oldSubscription.isPresent() && !oldSubscription.get().getId().equals(renewalSubscription.getId())) {
                oldSubscription.get().setStatus(SubscriptionStatus.ENDED);
                subscriptionRepository.save(oldSubscription.get());
                log.info("Ended old subscription {} for member {}", oldSubscription.get().getId(),
                        renewalSubscription.getMember().getId());
            }

        } else if (renewalSubscription.getFamily() != null) {
            // Find the old active subscription for this family
            Optional<Subscription> oldSubscription = subscriptionRepository
                    .findByFamilyIdAndStatusIn(renewalSubscription.getFamily().getId(),
                            Arrays.asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.DUE,
                                    SubscriptionStatus.IN_GRACE));

            if (oldSubscription.isPresent() && !oldSubscription.get().getId().equals(renewalSubscription.getId())) {
                oldSubscription.get().setStatus(SubscriptionStatus.ENDED);
                subscriptionRepository.save(oldSubscription.get());
                log.info("Ended old subscription {} for family {}", oldSubscription.get().getId(),
                        renewalSubscription.getFamily().getId());
            }
        }
    }

}
