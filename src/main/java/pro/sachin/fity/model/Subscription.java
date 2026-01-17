package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
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

    // TODO: CRITICAL - AUTO-CALCULATE in @PrePersist or Service layer
    // TODO: Formula: endDate = startDate + plan.durationDays
    // TODO: This should NEVER be provided by client!
    @Column(nullable = false, name = "end_date")
    private LocalDate endDate;

    // TODO: CRITICAL - AUTO-CALCULATE in @PrePersist or Service layer
    // TODO: Formula: dueDate = endDate (payment due on end date)
    // TODO: Alternative: dueDate = endDate - 7 days (payment due 7 days before end)
    // TODO: This should NEVER be provided by client!
    @Column(nullable = false, name = "due_date")
    private LocalDate dueDate;

    // TODO: CRITICAL - AUTO-CALCULATE in @PrePersist or Service layer
    // TODO: Formula: graceEndDate = endDate + 7 days
    // TODO: After this date, member gets BLOCKED
    // TODO: This should NEVER be provided by client!
    @Column(nullable = false, name = "grace_end_date")
    private LocalDate graceEndDate;

    // TODO: CRITICAL - AUTO-SET based on business rules:
    // TODO: On creation: ACTIVE
    // TODO: After endDate: IN_GRACE (automatic background job)
    // TODO: After graceEndDate: BLOCKED (automatic background job)
    // TODO: After renewal payment: ACTIVE again
    // TODO: Add @Enumerated(EnumType.STRING) annotation!
    @Column(nullable = false)
    private SubscriptionStatus status;

    // TODO: MISSING - Add createdByUserId (foreign key to Users table)
    // TODO: This tracks which staff member created the subscription
    // TODO: @ManyToOne relationship to User entity when it's created

    // TODO: Use @CreationTimestamp for automatic timestamp
    @Column(nullable = false, name = "subscribed_date")
    @CreationTimestamp
    private LocalDateTime createdAt;

    // TODO: MISSING - Add @OneToOne relationship to SubscriptionCharges
    // TODO: MISSING - Add @OneToMany relationship to Payments
    // TODO: MISSING - Add @OneToMany relationship to GraceExtensions
    
    // TODO: CRITICAL - Add constraint to prevent overlapping subscriptions
    // TODO: Check in service layer: no other ACTIVE subscription for same member/family

    // ============= VALIDATION LOGIC =============
    
    @PrePersist
    @PreUpdate
    private void validateRequestDetailsAtEntityLevel(){
        boolean hasMember = member != null;
        boolean hasFamily = family != null;

        if(hasMember == hasFamily){
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
    // TODO: FUTURE - Add helper method to check if fully paid (sum payments vs charge)
}
