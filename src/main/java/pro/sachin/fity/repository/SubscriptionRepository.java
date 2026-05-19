package pro.sachin.fity.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionStatus;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // this if for subscriptions that are passed end date and still active
    List<Subscription> findByEndDateBeforeAndStatus(LocalDate endDate, SubscriptionStatus status);

    // this if for subscriptions that are passed grace end date and still in grace
    // period
    List<Subscription> findByGraceEndDateBeforeAndStatus(LocalDate graceEndDate, SubscriptionStatus status);

    // check that member has an active subscription

    boolean existsByMemberIdAndStatus(Long memberId, SubscriptionStatus status);

    // check that family has an active subscription
    boolean existsByFamilyIdAndStatus(Long familyId, SubscriptionStatus status);

    // check that member has an active subscription for a given plan
    Optional<Subscription> findByMemberIdAndPlanIdAndStatus(Long memberId, Long planId, SubscriptionStatus status);

    // check that family has an active subscription for a given plan
    Optional<Subscription> findByFamilyIdAndPlanIdAndStatus(Long familyId, Long planId, SubscriptionStatus status);

    // this is for finding subscriptions fully paid before the end date

    List<Subscription> findByStatusAndStartDateBefore(SubscriptionStatus status, LocalDate startDate);

    List<Subscription> findByStatus(SubscriptionStatus status);

    // this is for subscrtiptions that are passed due date and still active
    List<Subscription> findByDueDateBeforeAndStatus(LocalDate dueDate, SubscriptionStatus status);

    List<Subscription> findByEndDate(LocalDate endDate);
}