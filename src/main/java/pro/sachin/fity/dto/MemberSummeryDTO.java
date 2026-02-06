package pro.sachin.fity.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import pro.sachin.fity.model.MemberStatus;

@Getter
@Setter
public class MemberSummeryDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private MemberStatus status;
    private LocalDateTime joinedDate;
}
