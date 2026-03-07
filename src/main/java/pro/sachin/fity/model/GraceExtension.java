package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "grace_extensions")
public class GraceExtension {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "extension_id")
    private Long id;
    
    // Which subscription is being extended
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    
    // Who authorized the extension (critical for accountability)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extended_by_user_id", nullable = false)
    private User extendedByUser;
    
    // Audit trail: old vs new dates
    @Column(name = "old_grace_end_date", nullable = false)
    private LocalDate oldGraceEndDate;
    
    @Column(name = "new_grace_end_date", nullable = false)
    private LocalDate newGraceEndDate;
    
    // Why was this granted
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    // When was this granted
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // TODO: Add helper method to calculate extension days
    // public long getExtensionDays() {
    //     return ChronoUnit.DAYS.between(oldGraceEndDate, newGraceEndDate);
    // }
}