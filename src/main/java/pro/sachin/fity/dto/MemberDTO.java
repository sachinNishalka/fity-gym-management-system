package pro.sachin.fity.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import pro.sachin.fity.model.MemberStatus;

@Data
public class MemberDTO {

    private Long id;

    private String firstName;

    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String phoneNumber;

    private String email;

    // Read-only field - automatically set by server during registration
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime joinedDate;

    // Read-only field - automatically set by server (should not be in request)
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
