package pro.sachin.fity.sercives.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.MemberAccess;
import pro.sachin.fity.repository.MemberAccessRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.sercives.MemberAccessService;

@Service
@RequiredArgsConstructor
public class MemberAccessImpl implements MemberAccessService{

    private final MemberAccessRepository memberAccessRepository;
    private final MemberRepository memberRepository;

    @Override
    public void updateMemberAccess(Long memberId, LocalDate allowedUntil, AccessStatus accessStatus, String reason) {
       
        if(memberId != null){
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("Member not found"));
            // if the record already present in the system use it
            if(member != null){
                MemberAccess memberAccess = memberAccessRepository.findById(member.getId()).orElseThrow(() -> new RuntimeException("MemberAccess not found"));
                // if member access present 
                if(memberAccess != null){
                    memberAccess.setReason(reason);
                    memberAccess.setAllowedUntil(allowedUntil);
                    memberAccess.setAccessStatus(accessStatus);
                    memberAccessRepository.save(memberAccess);
                }else{
                    MemberAccess newMemberAccess = new MemberAccess();
                    newMemberAccess.setMember(member);
                    newMemberAccess.setReason(reason);
                    newMemberAccess.setAllowedUntil(allowedUntil);
                    newMemberAccess.setAccessStatus(accessStatus);
                    memberAccessRepository.save(newMemberAccess);
                }

            }
            
        }

    }
    
}
