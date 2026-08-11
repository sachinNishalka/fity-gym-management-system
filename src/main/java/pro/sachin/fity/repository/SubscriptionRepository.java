package pro.sachin.fity.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionStatus;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

        // this if for subscriptions that are passed end date and still active
        List<Subscription> findByEndDateBeforeAndStatus(LocalDate endDate, SubscriptionStatus status);

        // this is for subscriptions that are passed end date and still active, and due
        @Query("select s from Subscription s where s.endDate < :endDate and s.status in :statusList")
        List<Subscription> findByEndDateBeforeAndStatusIn(@Param("endDate") LocalDate endDate,
                        @Param("statusList") List<SubscriptionStatus> statusList);

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

        Optional<Subscription> findByFamilyIdAndStatus(Long familyId, SubscriptionStatus status);

        @Query("SELECT DISTINCT s FROM Subscription s " +
                        "LEFT JOIN FETCH s.member m " +
                        "LEFT JOIN FETCH m.memberAccess " + // Also fetch memberAccess if needed
                        "LEFT JOIN FETCH s.family f " +
                        "LEFT JOIN FETCH s.plan p " +
                        "LEFT JOIN FETCH s.subscriptionCharges sc " +
                        "WHERE s.status IN ('DUE', 'ACTIVE', 'IN_GRACE') " +
                        "AND s.id IN (" +
                        "  SELECT s2.id FROM Subscription s2 " +
                        "  WHERE s2.status IN ('DUE', 'ACTIVE', 'IN_GRACE') " +
                        "  AND s2.startDate = (" +
                        "    SELECT MAX(s3.startDate) FROM Subscription s3 " +
                        "    WHERE s3.status IN ('DUE', 'ACTIVE', 'IN_GRACE') " +
                        "    AND ((s3.member IS NOT NULL AND s3.member = s2.member) " +
                        "         OR (s3.family IS NOT NULL AND s3.family = s2.family))" +
                        "  )" +
                        ")")
        List<Subscription> findRenwalSubscriptionList();

}