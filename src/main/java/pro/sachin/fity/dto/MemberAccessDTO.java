package pro.sachin.fity.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import pro.sachin.fity.model.AccessStatus;

@Getter
@Setter
public class MemberAccessDTO {

    private Long memberId;
    private String firstName;
    private String lastName;
    private LocalDate allowedUntil;
    private String reason;
    private String accessStatus;
}
