package pro.sachin.fity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import pro.sachin.fity.dto.PaymentDTO;
import pro.sachin.fity.model.Payments;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.PaymentService;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/payment")
@CrossOrigin
public class PaymentController {
    private final PaymentService paymentService;

    // TODO: CRITICAL - Repository should NOT be in controller - move to service
    private final SubscriptionRepository subscriptionRepository;

    // 2. If fully paid, update member_access to ALLOWED (unblock door)
    // 4. Generate receipt number if not provided
    // 5. Validate payment amount doesn't exceed balance due

    // TODO: Return PaymentResponseDTO with updated balance information

    @PostMapping("/save")
    ResponseEntity<PaymentDTO> savePayment(@RequestBody PaymentDTO paymentDTO) {

        paymentService.savePayment(paymentDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentDTO);
    }

    // TODO: IMPLEMENT - Record payment with automatic receipt generation
    // TODO: POST /api/v1/payment/record
    // TODO: Request: subscriptionId, amount, receivedByUserId, note
    // TODO: Auto-generate receiptNo in format: REC-YYYYMMDD-XXXXX
    // TODO: Auto-set paidOn to current timestamp
    // TODO: Return: payment details + updated subscription balance

    // TODO: IMPLEMENT - Get all payments for subscription
    // TODO: GET /api/v1/payment/subscription/{subscriptionId}
    // TODO: Return all payments ordered by paidOn DESC
    // TODO: Include running balance calculation

    // TODO: IMPLEMENT - Get payment by receipt number
    // TODO: GET /api/v1/payment/receipt/{receiptNo}
    // TODO: For customer verification and queries

    // TODO: IMPLEMENT - Get payments by date range
    // TODO: GET /api/v1/payment/date-range?from=2026-01-01&to=2026-01-31
    // TODO: For daily collection reports
    // TODO: Include total amount collected

    // TODO: IMPLEMENT - Get payments received by user
    // TODO: GET /api/v1/payment/received-by/{userId}?date=2026-01-17
    // TODO: For staff accountability and daily reconciliation

    // TODO: IMPLEMENT - Void/refund payment
    // TODO: POST /api/v1/payment/{id}/void
    // TODO: Requires reason and authorization
    // TODO: Update subscription balance and access status
    // TODO: Create audit trail entry

    // TODO: IMPLEMENT - Get payment summary
    // TODO: GET /api/v1/payment/summary?date=2026-01-17
    // TODO: Return: total collected, number of payments, breakdown by user

    // TODO: VALIDATION - Add @Valid annotation for request validation
    // TODO: VALIDATION - Ensure amount > 0
    // TODO: VALIDATION - Ensure subscription exists and is not ENDED
    // TODO: VALIDATION - Ensure payment doesn't exceed remaining balance
    // TODO: VALIDATION - Ensure receiptNo is unique

    // TODO: BUSINESS LOGIC - After payment triggers:
    // - If fully paid → Update member_access.access_status = 'ALLOWED'
    // - If subscription.status = 'BLOCKED' → Change to 'ACTIVE'
    // - Send payment confirmation notification (SMS/Email)
    // - Update notification_queue (cancel pending reminders if fully paid)

    // TODO: SECURITY - Track receivedByUserId (which staff took the payment)
    // TODO: SECURITY - Only authenticated staff can record payments
    // TODO: REPORTING - Integrate with daily collection reports
}
