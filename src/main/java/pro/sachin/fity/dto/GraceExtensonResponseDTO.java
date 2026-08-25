package pro.sachin.fity.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraceExtensonResponseDTO {

    private Long id;
    private Long subscriptionId;
    private Long memberId;
    private String memberCode;
    private LocalDate oldGraceEndDate;
    private LocalDate newGraceEndDate;
    private String reason;
    private LocalDateTime lastExtensionHappened;
    private String memberFirstName;
    private String memberLastName;
    private LocalDate originalPaymentDate;

}
