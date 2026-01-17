package pro.sachin.fity.dto;

import java.time.LocalDate;

import lombok.Data;

// TODO: CRITICAL - This DTO should probably NOT exist as a separate request DTO
// TODO: Subscription charges should be created automatically when subscription is created
// TODO: If you need this, it should be RESPONSE ONLY (for viewing charge details)

@Data
public class SubscriptionChargesDTO {
    
    // TODO: If using for request - subscriptionId is required
    // TODO: Add validation: @NotNull
    private Integer subscriptionId;
    
    // TODO: RESPONSE ONLY - For display purposes
    private String subscriptionName;
    
    // TODO: CRITICAL - Change from 'int' to 'BigDecimal' for accurate money handling
    // TODO: This should NEVER be provided by client - auto-calculated from subscription.plan.price
    // TODO: Should be RESPONSE ONLY
    private int totalAmount;
    
    // TODO: CRITICAL - Change from 'int' to 'BigDecimal'
    // TODO: This can be provided in request (manual discount)
    // TODO: Or calculated by discount rules (future feature)
    // TODO: Add validation: @PositiveOrZero, must be <= totalAmount
    private int discountAmount;
    
    // TODO: CRITICAL - Change from 'int' to 'BigDecimal'
    // TODO: This should NEVER be provided by client - auto-calculated
    // TODO: Formula: netAmount = totalAmount - discountAmount
    // TODO: Should be RESPONSE ONLY
    // TODO: Add validation: must be > 0
    private int netAmount;
    
    // TODO: RESPONSE ONLY - Add more fields for complete charge details:
    //       - chargeId
    //       - createdAt
    //       - paidAmount (sum of all payments)
    //       - balanceDue (netAmount - paidAmount)
    //       - isFullyPaid (boolean)
    //       - paymentCount (number of payments made)
    
    // TODO: BETTER APPROACH - This should be part of SubscriptionResponseDTO
    // TODO: Don't create charges separately from subscription
    // TODO: When subscription is created, charges are automatically created
    // TODO: Only need GET endpoint to view charges, not POST to create
    
    // TODO: IF you need discount adjustment endpoint:
    // TODO: Create ApplyDiscountRequest DTO:
    //       - subscriptionId (required)
    //       - discountAmount OR discountPercentage
    //       - reason (for audit)
    //       - appliedByUserId (who approved discount)
}

