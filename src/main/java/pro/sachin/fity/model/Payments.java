package pro.sachin.fity.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Id;

import lombok.Data;

@Entity
@Data
public class Payments {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // TODO: This relationship is correct - multiple payments per subscription
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    
    
    // TODO: Add validation: amount must be positive (@Positive annotation)
    // TODO: Add validation: amount cannot exceed remaining balance due
    @Column(nullable = false, name = "amount")
    private BigDecimal amount;

    // TODO: SQL uses TIMESTAMP not DATE - change to LocalDateTime
    // TODO: Use @CreationTimestamp annotation instead of default value
    @Column(nullable = false, name = "paid_on")
    @CreationTimestamp
    private LocalDateTime paidOn;

    // TODO: CRITICAL - Add receivedByUserId field (foreign key to Users table)
    // TODO: @ManyToOne relationship to User entity
    // TODO: This tracks which staff member (coach/receptionist) received the payment

    // TODO: Receipt number might be generated after payment is recorded
    @Column(nullable = true, name = "receipt_no")
    private String receiptNo;


    private String note;
    
    
    // TODO: 3. If fully paid, update member_access to ALLOWED (unblock door)

    
    // TODO: VALIDATION - Prevent duplicate receipt numbers (add unique constraint)
    // TODO: VALIDATION - Prevent payments for subscriptions that haven't been created yet
    
    // TODO: FUTURE - Add payment method field (CASH, CARD, BANK_TRANSFER) even though only cash for now
    // TODO: FUTURE - Add refund support (negative amounts or separate Refund entity)
}
