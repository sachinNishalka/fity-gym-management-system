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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class SubscriptionCharges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false, unique = true)
    private Subscription subscription;
    
   
    // TODO: This should be AUTO-CALCULATED from subscription.plan.price
    // TODO: Should NEVER be provided by client!
    @Column(nullable = false, name = "total_amount")
    private BigDecimal totalAmount;

    // TODO: This can be provided by client (manual discount) or calculated by discount rules
    // TODO: Default value should be 0
    @Column(nullable = false, name = "discount_amount")
    private BigDecimal discountAmount;
    
 
    // TODO: This should be AUTO-CALCULATED: netAmount = totalAmount - discountAmount
    // TODO: Should NEVER be provided by client!
    // TODO: Add validation: netAmount must be > 0
    @Column(nullable = false, name = "net_amount")
    private BigDecimal netAmount;
    

    @Column(nullable = false, name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // TODO: CRITICAL - This entity should be created AUTOMATICALLY when subscription is created
    // TODO: Should be created in the same transaction as subscription (atomic operation)
    
    // TODO: FUTURE - Add helper method to calculate balance due
    // TODO: Formula: balanceDue = netAmount - SUM(payments.amount)
    // TODO: Need to query Payment repository for this

}
