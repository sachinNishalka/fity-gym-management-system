package pro.sachin.fity.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberAccessDTO {

    private int memberCode;
    private String memberName;
    private LocalDate allowedUntil;
    private Boolean accessStatus;
}
