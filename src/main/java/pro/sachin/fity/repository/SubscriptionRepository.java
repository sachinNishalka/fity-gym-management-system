package pro.sachin.fity.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pro.sachin.fity.model.Plan;
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

        // Add these methods to SubscriptionRepository interface:

        // 1. Find subscriptions by multiple statuses and start date (for scheduler)
        List<Subscription> findByStatusInAndStartDateBefore(List<SubscriptionStatus> statuses, LocalDate startDate);

        // 2. Find member subscription by multiple statuses (for ending old
        // subscriptions)
        Optional<Subscription> findByMemberIdAndStatusIn(Long memberId, List<SubscriptionStatus> statuses);

        // 3. Find family subscription by multiple statuses (for ending old
        // subscriptions)
        Optional<Subscription> findByFamilyIdAndStatusIn(Long familyId, List<SubscriptionStatus> statuses);

        // 4. Optional: Find top/first subscription by member and statuses ordered by
        // creation date
        Optional<Subscription> findFirstByMemberIdAndStatusInOrderByCreatedAtDesc(Long memberId,
                        List<SubscriptionStatus> statuses);

        // 5. Optional: Find top/first subscription by family and statuses ordered by
        // creation date
        Optional<Subscription> findFirstByFamilyIdAndStatusInOrderByCreatedAtDesc(Long familyId,
                        List<SubscriptionStatus> statuses);

        List<Subscription> findByEndDate(LocalDate endDate);

        Subscription findByMemberIdAndStatus(Long memberId, SubscriptionStatus active);

        Subscription findByFamilyId(Long familyId);

        @Query(value = "SELECT * FROM subscription WHERE status = 'DUE' OR status = 'ACTIVE' OR status = 'IN_GRACE'", nativeQuery = true)
        List<Subscription> findRenwalSubscriptionList();

        List<Subscription> findByPlan(Plan plan);

}