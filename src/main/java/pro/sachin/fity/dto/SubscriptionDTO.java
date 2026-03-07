package pro.sachin.fity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// TODO: CRITICAL - This DTO has wrong purpose - it's mixing request and response
// TODO: Split into two separate DTOs:
//       1. SubscriptionEnrollmentRequest (for creating subscription)
//       2. SubscriptionResponseDTO (for returning subscription data)

@Data
public class SubscriptionDTO {

    // TODO: RESPONSE ONLY - Don't include in request DTO
    private Long id;

    // TODO: REQUEST - Only provide ONE of memberId or familyId, never both
    // TODO: Add validation: @NotNull when creating subscription
    private Long memberId;

    // TODO: RESPONSE ONLY - For display purposes
    private String memberName;

    // TODO: REQUEST - Only provide ONE of memberId or familyId, never both
    private Long familyId;

    // TODO: RESPONSE ONLY - For display purposes
    private String familyName;

    // TODO: REQUEST - Required field for subscription
    // TODO: Add validation: @NotNull
    private Long planId;

    // TODO: RESPONSE ONLY - For display purposes
    private String planName;

    // TODO: REQUEST - Optional, defaults to today if not provided
    private LocalDate startDate;

    // TODO: CRITICAL - Should NEVER be in request - auto-calculated from startDate
    // + plan.durationDays
    // TODO: RESPONSE ONLY
    private LocalDate endDate;

    // TODO: CRITICAL - Should NEVER be in request - auto-calculated (usually same
    // as endDate)
    // TODO: RESPONSE ONLY
    private LocalDate dueDate;

    // TODO: CRITICAL - Should NEVER be in request - auto-calculated as endDate + 7
    // days
    // TODO: RESPONSE ONLY
    private LocalDate graceEndDate;

    // TODO: CRITICAL - Should NEVER be in request - auto-set to ACTIVE on creation
    // TODO: RESPONSE ONLY - Can be: ACTIVE, IN_GRACE, BLOCKED, ENDED
    private String status;

    // TODO: MISSING - Add discountAmount field for request
    // TODO: Optional field, defaults to 0 if not provided

    // TODO: MISSING - Add initialPaymentAmount field for request
    // TODO: Optional field for immediate payment during enrollment

    // TODO: RESPONSE ONLY - Add calculated fields:
    // - totalAmount (from charges)
    // - netAmount (from charges)
    // - paidAmount (sum of payments)
    // - balanceDue (netAmount - paidAmount)
    // - daysRemaining (days until endDate)
    // - daysInGrace (if in grace period)

    // TODO: BETTER APPROACH - Create separate DTOs:
    // TODO: 1. SubscriptionEnrollmentRequest
    // - memberId OR familyId (validated mutually exclusive)
    // - planId (required)
    // - startDate (optional, default today)
    // - discountAmount (optional, default 0)
    // - initialPaymentAmount (optional)
    //
    // TODO: 2. SubscriptionResponseDTO
    // - id
    // - memberName OR familyName
    // - planName
    // - All dates (start, end, due, graceEnd)
    // - status
    // - Financial info (total, net, paid, balance)
    // - Days remaining/in grace
    //
    // TODO: 3. SubscriptionListDTO (for list views)
    // - Minimal fields for performance
    // - id, memberName, planName, endDate, status, balanceDue

    private BigDecimal discountAmount;

    // added after finding the workflow issue in the registration process
    private BigDecimal initialPaymentAmount;

    // this is a receipt number for initial payment, this is not neccessary
    private String initialReceiptNumber;
}
