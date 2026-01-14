package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int Id;

    @Column(nullable = false, name = "name")
    private String name;
//    TODO: change this to a enum type
    @Column(nullable = false, name = "duration_days")
    private int durationDays;
    @Column(nullable = false)
    private double price;
    @Enumerated(value = EnumType.STRING )
    private PlanType planType;
    @Column(name = "age_min")
    private int ageMin;
    @Column(name = "age_max")
    private int ageMax;
    @Column(name = "maximum_family_members")
    private int maximumFamilyMembers;
}
