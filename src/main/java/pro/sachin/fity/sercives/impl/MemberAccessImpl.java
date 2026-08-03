package pro.sachin.fity.sercives.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import pro.sachin.fity.dto.GymAccessDTO;
import pro.sachin.fity.dto.MemberAccessDTO;
import pro.sachin.fity.mapper.MemberAccessMapper;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.MemberAccess;
import pro.sachin.fity.repository.MemberAccessRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.sercives.DeviceCommandService;
import pro.sachin.fity.sercives.MemberAccessService;

@Service
@RequiredArgsConstructor
public class MemberAccessImpl implements MemberAccessService {

  private final MemberAccessRepository memberAccessRepository;
  private final MemberRepository memberRepository;
  private final DeviceCommandService deviceCommandService;
  private final MemberAccessMapper memberAccessMapper;

  @Override
  public void updateMemberAccess(Long memberId, LocalDate allowedUntil, AccessStatus accessStatus, String reason) {

    // if(memberId != null){
    // Member member = memberRepository.findById(memberId).orElseThrow(() -> new
    // RuntimeException("Member not found"));
    // // if the record already present in the system use it
    // if(member != null){
    // MemberAccess memberAccess =
    // memberAccessRepository.findById(member.getId()).orElseThrow(() -> new
    // RuntimeException("MemberAccess not found"));
    // // if member access present
    // if(memberAccess != null){
    // memberAccess.setReason(reason);
    // memberAccess.setAllowedUntil(allowedUntil);
    // memberAccess.setAccessStatus(accessStatus);
    // memberAccessRepository.save(memberAccess);
    // }else{
    // MemberAccess newMemberAccess = new MemberAccess();
    // newMemberAccess.setMember(member);
    // newMemberAccess.setReason(reason);
    // newMemberAccess.setAllowedUntil(allowedUntil);
    // newMemberAccess.setAccessStatus(accessStatus);
    // memberAccessRepository.save(newMemberAccess);
    // }

    // }

    // }

    if (memberId == null) {
      throw new IllegalArgumentException("Member cannot be null");
    }

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new EntityNotFoundException("Member not found with id " + memberId));
    MemberAccess memberAccess = memberAccessRepository.findById(memberId).orElse(new MemberAccess());
    memberAccess.setMember(member);
    memberAccess.setReason(reason);
    memberAccess.setAllowedUntil(allowedUntil);
    memberAccess.setAccessStatus(accessStatus);
    memberAccessRepository.save(memberAccess);

    deviceCommandService.queueAccessUpdate(memberId, member.getMemberCode(), accessStatus, allowedUntil);

  }

  @Override
  public List<MemberAccess> getAllMemberAccess() {
    return memberAccessRepository.findAllWithSubscriptionAndMember();
  }
}
