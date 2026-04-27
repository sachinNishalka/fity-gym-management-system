package pro.sachin.fity.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import pro.sachin.fity.model.AccessStatus;

@Getter
@Setter
public class MemberAccessDTO {

    private String memberCode;
    private String memberName;
    private LocalDate allowedUntil;
    private AccessStatus accessStatus;
}
