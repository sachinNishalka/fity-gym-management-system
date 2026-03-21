package pro.sachin.fity.scheduler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        moveToGracePeriod(today);

        blockExpiredSubscriptions(today);

        activatePaidRenewals();

        moveToDueStatus(today);

    }

    public void moveToGracePeriod(LocalDate today) {
        List<Subscription> subscriptions = subscriptionRepository.findByEndDateBeforeAndStatus(today,
                SubscriptionStatus.ACTIVE);

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

    public void blockExpiredSubscriptions(LocalDate today) {
        List<Subscription> subscriptions = subscriptionRepository.findByGraceEndDateBeforeAndStatus(today,
                SubscriptionStatus.IN_GRACE);

        if (!subscriptions.isEmpty()) {
            subscriptions.forEach(subscription -> {
                subscription.setStatus(SubscriptionStatus.BLOCKED);

                if (subscription.getMember() != null) {
                    memberAccessService.updateMemberAccess(subscription.getMember().getId(), today,
                            AccessStatus.BLOCKED, "Grace period expired");
                } else if (subscription.getFamily() != null) {
                    // TODO: this family subscription should be blocked

                    Family family = subscription.getFamily();

                    for (Member member : family.getMembers()) {
                        memberAccessService.updateMemberAccess(member.getId(), today, AccessStatus.BLOCKED,
                                "Family subscription expired!");
                        log.info("Blocked access for {} in family {}", member.getFirstName(), family.getFamilyName());
                    }

                }
            });
        }
        subscriptionRepository.saveAll(subscriptions);
        log.info("Blocked expired subscriptions {} ", subscriptions.size());

    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void activatePaidRenewals() {
        LocalDate today = LocalDate.now();

        List<Subscription> pendingRenewals = subscriptionRepository
                .findByStatusAndStartDateBefore(SubscriptionStatus.PENDING, today.plusDays(1));

        for (Subscription renewal : pendingRenewals) {
            if (isSubscriptionFullyPaid(renewal.getId())) {

                // setting the subscription status to active
                renewal.setStatus(SubscriptionStatus.ACTIVE);

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

}
