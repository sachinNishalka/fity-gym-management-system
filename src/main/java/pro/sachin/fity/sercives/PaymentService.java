package pro.sachin.fity.sercives;

import pro.sachin.fity.dto.PaymentDTO;
import pro.sachin.fity.model.Payments;

public interface PaymentService {
    // TODO: CRITICAL - This method is missing post-payment business logic
    // TODO: After saving payment, should trigger:
    //       1. Check if subscription is fully paid
    //       2. If fully paid AND subscription.status = BLOCKED → change to ACTIVE
    //       3. If fully paid → update member_access.access_status = ALLOWED
    //       4. Send payment confirmation notification
    //       5. Cancel pending reminder notifications if fully paid
    // TODO: Use @Transactional to ensure atomic updates
    void savePayment(PaymentDTO paymentDTO);
    
    // TODO: IMPLEMENT - Record payment with auto-generated receipt
    // TODO: PaymentResponseDTO recordPayment(PaymentRecordRequest request);
    // TODO: Request fields: subscriptionId, amount, receivedByUserId, note
    // TODO: Auto-generate receiptNo: format REC-YYYYMMDD-XXXXX
    // TODO: Auto-set paidOn to current timestamp
    // TODO: Validate amount doesn't exceed balance due
    // TODO: Trigger post-payment logic (balance check, access update)
    // TODO: Return payment details + updated subscription balance
    
    // TODO: IMPLEMENT - Get payments for subscription
    // TODO: List<PaymentDTO> getPaymentsForSubscription(Long subscriptionId);
    // TODO: Return all payments ordered by paidOn DESC
    // TODO: Include running balance calculation
    
    // TODO: IMPLEMENT - Get payment by receipt number
    // TODO: PaymentDTO getPaymentByReceipt(String receiptNo);
    // TODO: For customer verification and queries
    // TODO: Return null if not found
    
    // TODO: IMPLEMENT - Get payments by date range
    // TODO: PaymentSummaryDTO getPaymentsByDateRange(LocalDate from, LocalDate to);
    // TODO: Return list of payments in date range
    // TODO: Include total amount collected
    // TODO: For daily collection reports
    
    // TODO: IMPLEMENT - Get payments received by user
    // TODO: List<PaymentDTO> getPaymentsByReceivedUser(Long userId, LocalDate date);
    // TODO: For staff accountability report
    // TODO: Show all payments collected by specific staff member
    
    // TODO: IMPLEMENT - Get daily payment summary
    // TODO: DailyPaymentSummary getDailySummary(LocalDate date);
    // TODO: Return: total amount, number of payments, breakdown by user
    // TODO: For end-of-day reconciliation
    
    // TODO: IMPLEMENT - Void/refund payment
    // TODO: void voidPayment(Long paymentId, String reason, Long voidedByUserId);
    // TODO: Mark payment as void/refunded
    // TODO: Recalculate subscription balance
    // TODO: Update member access if balance becomes unpaid
    // TODO: Create audit trail entry
    // TODO: Requires authorization (admin only)
    
    // TODO: IMPLEMENT - Calculate total paid for subscription
    // TODO: BigDecimal getTotalPaidAmount(Long subscriptionId);
    // TODO: SUM(amount) for all payments for subscription
    // TODO: Helper method for balance calculations
    
    // TODO: IMPLEMENT - Validate payment amount
    // TODO: void validatePaymentAmount(Long subscriptionId, BigDecimal amount);
    // TODO: Check amount > 0
    // TODO: Check amount doesn't exceed remaining balance due
    // TODO: Throw exception if invalid
    
    // TODO: IMPLEMENT - Check if subscription fully paid
    // TODO: boolean isSubscriptionFullyPaid(Long subscriptionId);
    // TODO: Compare total payments vs subscription charges net_amount
    // TODO: Used to trigger access updates
    
    // TODO: IMPLEMENT - Update member access after payment
    // TODO: void updateMemberAccessAfterPayment(Long subscriptionId);
    // TODO: If fully paid → set member_access.access_status = ALLOWED
    // TODO: If subscription was BLOCKED → change to ACTIVE
    // TODO: Update member_access.allowed_until = subscription.grace_end_date
    // TODO: This is CRITICAL for door system integration
    
    // TODO: IMPLEMENT - Generate receipt number
    // TODO: String generateReceiptNumber();
    // TODO: Format: REC-YYYYMMDD-XXXXX (where XXXXX is daily sequence)
    // TODO: Ensure uniqueness
    // TODO: Consider format: REC-20260117-00001
    
    // TODO: VALIDATION - Prevent duplicate receipt numbers
    // TODO: VALIDATION - Ensure payment is for valid subscription
    // TODO: VALIDATION - Track who received payment (receivedByUserId)
    // TODO: SECURITY - Only authenticated staff can record payments
    // TODO: AUDIT - Log all payment operations with timestamp and user
}

