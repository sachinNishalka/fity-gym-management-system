package pro.sachin.fity.sercives;

import java.time.LocalDate;

import pro.sachin.fity.model.AccessStatus;

public interface MemberAccessService {

    void updateMemberAccess(Long memberId, LocalDate allowedUntil, AccessStatus accessStatus, String reason);
    
}
