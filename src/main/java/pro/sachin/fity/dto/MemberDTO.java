package pro.sachin.fity.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.MemberAccess;
import pro.sachin.fity.model.MemberStatus;

@Data
public class MemberDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private String phoneNumber;

    private String email;

    private LocalDateTime joinedDate;

    private MemberStatus status;

    private String gender;

    private String memberCode;

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

    private String memberAccessStatus;
}
