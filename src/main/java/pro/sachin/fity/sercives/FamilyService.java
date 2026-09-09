package pro.sachin.fity.sercives;

import java.util.List;

import pro.sachin.fity.dto.FamilyDTO;
import pro.sachin.fity.dto.FamilyResponseDTO;
import pro.sachin.fity.dto.MemberSummeryDTO;
import pro.sachin.fity.dto.PlanDTO;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.model.Family;

public interface FamilyService {
    // TODO: create family
    Family createFamily(FamilyDTO familyDTO);

    FamilyResponseDTO getFamilyById(Long id);

    List<FamilyResponseDTO> getAllFamilies();

    List<MemberSummeryDTO> getFamilyMembers(Long familyId);

    FamilyResponseDTO addMemberToFamily(Long familyId, Long memberId);

    FamilyResponseDTO updateFamilyName(Long familyId, String familyName);

    void removeMemberFromFamily(Long familyId, Long memberId);

    void deleteFamily(Long familyId);

    List<SubscriptionDTO> getAllSubscriptionsForFamily(Long familyId);

    List<PlanDTO> getEligiblePlansForFamily(Long familyId);
}
