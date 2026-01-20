package pro.sachin.fity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

// TODO: CRITICAL - This DTO is mixing request and response fields
// TODO: Split into PaymentRecordRequest and PaymentResponseDTO

@Data
public class PaymentDTO  {
    
    // TODO: REQUEST - Required field
    // TODO: Add validation: @NotNull, @Positive
    private Long subscriptionId;
    
    // TODO: RESPONSE ONLY - For display purposes
    // TODO: Should show member/family name, not "subscription name"
    private String subscriptionName;
    
    // TODO: REQUEST - Required field
    // TODO: CRITICAL - Change from 'double' to 'BigDecimal' for accurate money handling
    // TODO: Add validation: @NotNull, @Positive, @DecimalMin("0.01")
    // TODO: Add validation: amount should not exceed balance due
    private BigDecimal amount;
    
    // TODO: CRITICAL - Should NOT be in request - auto-set to current timestamp
    // TODO: RESPONSE ONLY
    // TODO: Change to LocalDateTime to match SQL TIMESTAMP type
    // private LocalDate paidOn;
    
    // TODO: CRITICAL - Receipt number should be AUTO-GENERATED, not provided by client
    // TODO: Can be optional in request (if not provided, generate automatically)
    // TODO: Format: REC-YYYYMMDD-XXXXX
    // TODO: Fix typo: "recieptNo" → "receiptNo"
    private String recieptNo;
    
    // TODO: REQUEST - Optional field for payment notes
    // TODO: Change to "notes" (plural) to match SQL schema
    private String note;
    
    // TODO: MISSING - Add receivedByUserId field (REQUEST - required)
    // TODO: Track which staff member received the payment
    // TODO: This is CRITICAL for accountability and audit trail
    
    // TODO: MISSING - Add receivedByUserName field (RESPONSE ONLY)
    // TODO: For display purposes
    
    // TODO: RESPONSE ONLY - Add calculated fields:
    //       - paymentId (the ID of payment record)
    //       - balanceBeforePayment
    //       - balanceAfterPayment
    //       - subscriptionStatus (updated status after payment)
    //       - isFullyPaid (boolean)
    
    // TODO: BETTER APPROACH - Create separate DTOs:
    // TODO: 1. PaymentRecordRequest
    //          - subscriptionId (required)
    //          - amount (required, BigDecimal)
    //          - receivedByUserId (required)
    //          - notes (optional)
    //          - receiptNo (optional, auto-generated if not provided)
    //
    // TODO: 2. PaymentResponseDTO
    //          - paymentId
    //          - subscriptionId
    //          - memberName / familyName
    //          - amount
    //          - paidOn (LocalDateTime)
    //          - receiptNo
    //          - receivedByUserName
    //          - notes
    //          - balanceBeforePayment
    //          - balanceAfterPayment
    //          - isFullyPaid
    //
    // TODO: 3. PaymentListDTO (for payment history)
    //          - Minimal fields
    //          - paymentId, paidOn, amount, receiptNo, receivedByUserName
    //
    // TODO: 4. PaymentSummaryDTO (for reports)
    //          - totalAmount
    //          - paymentCount
    //          - breakdownByUser (Map<String, BigDecimal>)
    //          - date
}

