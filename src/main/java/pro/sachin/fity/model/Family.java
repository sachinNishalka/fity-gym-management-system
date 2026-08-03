package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "members")
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Consider making this nullable (SQL schema allows NULL)
    // TODO: Family name might be auto-generated like "Smith Family"
    @Column(nullable = false, name = "family_name")
    private String familyName;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // TODO: CRITICAL - MISSING - Add @ManyToMany relationship to Members

    // this would be one to many for now, becuase we are only starting out
    @OneToMany(mappedBy = "family")
    private Set<Member> members = new HashSet<>();

    // TODO: This needs a junction table: family_members (family_id, member_id,
    // role)
    // TODO: The 'role' field indicates: 'primary', 'spouse', 'child', 'member'
    // TODO: Example: @ManyToMany with @JoinTable annotation

    // TODO: MISSING - Add @OneToMany relationship to Subscriptions

    // TODO: VALIDATION - When family subscribes to a plan:
    // TODO: 1. Check plan.planType == FAMILY
    // TODO: 2. Check number of members <= plan.maximumFamilyMembers
    // TODO: 3. Ensure at least 2 members in family (business rule)

    // TODO: FUTURE - Add helper method to get primary member
    // TODO: FUTURE - Add helper method to count family members
    // TODO: FUTURE - Add helper method to validate if can subscribe to specific
    // plan
}
