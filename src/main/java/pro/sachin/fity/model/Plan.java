package pro.sachin.fity.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Add validation - name should be unique and have max length
    @Column(nullable = false, name = "name")
    private String name;
    
    // TODO: YES - Change this to DurationDays enum type (30, 90, 180 days)
    // TODO: This will make it easier to standardize plan durations
    @Column(nullable = false, name = "duration_days")
    private int durationDays;

    // TODO: Add validation - price must be positive (@Positive annotation)
    @Column(nullable = false)
    private BigDecimal price;
 
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, name = "plan_type")
    private PlanType planType;
    
    // TODO: These fields should only be used for KIDS plan type
    // TODO: Add validation to ensure ageMin < ageMax
    @Column(name = "age_min")
    private int ageMin;
    
    @Column(name = "age_max")
    private int ageMax;
    
    // TODO: This field should only be used for FAMILY plan type
    // TODO: Add validation to ensure maximumFamilyMembers > 1 for family plans
    @Column(name = "maximum_family_members")
    private int maximumFamilyMembers;
    


    @Column(nullable = false, name = "is_active")
    private boolean isActive;


    // TODO: This allows soft deletion of plans without breaking existing subscriptions
    
    // TODO: MISSING - Add @OneToMany relationship to Subscriptions
    
    // TODO: FUTURE - Add @PrePersist validation to ensure:
    //       - KIDS plans have ageMin and ageMax set
    //       - FAMILY plans have maximumFamilyMembers set
    //       - Age ranges are logical (min < max)
}
