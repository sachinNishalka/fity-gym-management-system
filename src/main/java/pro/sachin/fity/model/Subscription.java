package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Entity
@Check(constraints = "(member_id IS NOT NULL AND family_id IS NULL) OR (member_id IS NULL AND family_id IS NOT NULL)")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    // ============= DATES - CRITICAL BUSINESS LOGIC =============

    // TODO: This is the only date that should be provided by user/frontend
    @Column(nullable = false, name = "start_date")
    private LocalDate startDate;

    @Column(nullable = false, name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false, name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false, name = "grace_end_date")
    private LocalDate graceEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    // TODO: MISSING - Add createdByUserId (foreign key to Users table)
    // TODO: This tracks which staff member created the subscription
    // TODO: @ManyToOne relationship to User entity when it's created

    @Column(nullable = false, name = "subscribed_date")
    @CreationTimestamp
    private LocalDateTime createdAt;

    // TODO: MISSING - Add @OneToOne relationship to SubscriptionCharges
    @OneToOne(mappedBy = "subscription", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private SubscriptionCharges subscriptionCharges;

    // TODO: MISSING - Add @OneToMany relationship to Payments

    // TODO: MISSING - Add @OneToMany relationship to GraceExtensions

    // ============= VALIDATION LOGIC =============

    @OneToMany(mappedBy = "id", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Payments> payments;

    @PrePersist
    @PreUpdate
    private void validateRequestDetailsAtEntityLevel() {
        boolean hasMember = member != null;
        boolean hasFamily = family != null;

        if (hasMember == hasFamily) {
            // TODO: YES - Create custom exception: InvalidSubscriptionException
            // TODO: Handle globally with @ControllerAdvice
            throw new IllegalStateException("Subscription must have either member or family, not both");
        }

        // TODO: ADD - Auto-calculate dates here in @PrePersist
        // TODO: ADD - Validate plan type matches member/family type
        // TODO: ADD - If KIDS plan, validate member age is within plan's age range
        // TODO: ADD - If FAMILY plan, validate family has correct number of members
    }

    // TODO: FUTURE - Add helper method to check if subscription is expired
    // TODO: FUTURE - Add helper method to check if in grace period
    // TODO: FUTURE - Add helper method to calculate remaining days
    // TODO: FUTURE - Add helper method to check if fully paid (sum payments vs
    // charge)
}
