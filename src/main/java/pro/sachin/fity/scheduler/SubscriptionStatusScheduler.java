package pro.sachin.fity.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.MemberAccess;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionStatus;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.MemberAccessService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionStatusScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberAccessService memberAccessService;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void updateSubscriptionStatuses() {

        LocalDate today = LocalDate.now();
        log.info("Updating subscription statuses for today: {}", today);
        moveToGracePeriod(today);

        blockExpiredSubscriptions(today);

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

}
