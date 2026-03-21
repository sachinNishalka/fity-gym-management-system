package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Data

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
    // TODO: MISSING - Add @OneToOne relationship to MemberAccess (for door control)
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
    private String gender;  // "male" or "female" or "other"

    @Column(unique = true, name = "member_code")
    private String memberCode; // this is the code that will be used to identify the member
}
