package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.node.BooleanNode;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = { "subscriptions", "memberAccess", "family" }) // Exclude lazy fields
@EqualsAndHashCode(exclude = { "subscriptions", "memberAccess", "family" }) // Exclude lazy fields

public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Column(nullable = false, name = "last_name")
    private String lastName;

    // TODO: Consider adding @Past validation annotation to ensure DOB is in the
    // past
    // TODO: This field is needed for age-based plan eligibility (kids plans)
    private LocalDate dateOfBirth;

    // TODO: Add validation for phone number format (regex pattern)
    @Column(nullable = false, name = "phone_number")
    private String phoneNumber;

    // TODO: Add @Email validation annotation
    // TODO: Consider making this unique if business requires
    private String email;

    // TODO: Use @CreationTimestamp instead of default value for better JPA handling
    // TODO: Or use @PrePersist to set this value
    @Column(nullable = false, name = "joined_date")
    @CreationTimestamp
    private LocalDateTime joinedDate;

    // TODO: Set default value to 'ACTIVE' when member is first created
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private MemberStatus status;

    // TODO: MISSING - Add @OneToMany relationship to Subscriptions

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "member", fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;

    // TODO: MISSING - Add @OneToOne relationship to MemberAccess (for door control)

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "member", fetch = FetchType.LAZY)
    private MemberAccess memberAccess;

    // TODO: MISSING - Add @OneToMany relationship to Attendance records

    // TODO: MISSING - Add @ManyToMany relationship to Family (through
    // family_members junction)
    // for this initial functionality, this would be enough
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    // TODO: FUTURE - Consider adding helper method to calculate age from DOB
    // TODO: FUTURE - Consider adding helper method to check if eligible for
    // specific plan type

    @Column(name = "gender")
    private String gender; // "male" or "female" or "other"

    @Column(unique = true, name = "member_code")
    private String memberCode; // this is the code that will be used to identify the member

    @Column(name = "address")
    private String address;

    private float height;

    private float weight;

    private String idNumber;

    private String emergencyNumber;

    private String facebookName;

    private Boolean bodyBuilding;

    private Boolean fatBurning;

    private Boolean physicalFitness;

    private Boolean sportsSkills;

    private Boolean bodyShape;

    private String otherService;

    private Boolean cholesterol;

    private Boolean bloodPressure;

    private Boolean diabetes;

    private Boolean heartProblem;

    private Boolean surgery;

    private Boolean fractures;

    private Boolean kidneyLiver;

    private Boolean otherDisease;

    private Boolean currentTreatment;

}
