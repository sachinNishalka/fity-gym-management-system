package pro.sachin.fity.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class MemberDTO {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String email;
    private String gender;  // add this
    private String memberCode;
}
