package pro.sachin.fity.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FamilyDTO {

    private String familyName;
    private List<Long> memberIds;
}
