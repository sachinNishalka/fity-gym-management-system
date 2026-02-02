package pro.sachin.fity.sercives;

import java.util.List;

import pro.sachin.fity.dto.FamilyDTO;
import pro.sachin.fity.model.Family;

public interface FamilyService {
    // TODO: create family
    Family createFamily(FamilyDTO familyDTO);

    Family getFamilyById(Long id);
}
