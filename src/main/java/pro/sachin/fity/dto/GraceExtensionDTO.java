package pro.sachin.fity.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraceExtensionDTO {

    private Long subscriptionId;
    private LocalDate newGraceEndDate;
    private String reason;
    
}
