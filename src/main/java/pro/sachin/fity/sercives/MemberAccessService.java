package pro.sachin.fity.sercives;

import java.time.LocalDate;
import java.util.List;

import pro.sachin.fity.dto.MemberAccessDTO;
import pro.sachin.fity.model.AccessStatus;

public interface MemberAccessService {

    void updateMemberAccess(Long memberId, LocalDate allowedUntil, AccessStatus accessStatus, String reason);

    List<MemberAccessDTO> getAllMembersAccess();

    List<MemberAccessDTO> getAllDeniedMemberAccess();

}
