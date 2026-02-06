package pro.sachin.fity.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FamilyResponseDTO {

    private Long id;
    private String familyName;
    private LocalDateTime createdAt;
    private List<MemberSummeryDTO> members;
}
