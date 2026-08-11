package pro.sachin.fity.sercives.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.sachin.fity.mapper.MemberAccessMapper;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.MemberAccess;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionStatus;
import pro.sachin.fity.repository.MemberAccessRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.DeviceCommandService;
import pro.sachin.fity.sercives.MemberAccessService;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberAccessImpl implements MemberAccessService {

  private final MemberAccessRepository memberAccessRepository;
  private final MemberRepository memberRepository;
  private final DeviceCommandService deviceCommandService;
  private final MemberAccessMapper memberAccessMapper;
  private final SubscriptionRepository subscriptionRepository;

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

  @Override
  @Transactional
  public List<String> recoverIncorrectlyBlockedMembers() {
    List<MemberAccess> blockedRecords = memberAccessRepository.findAllBlockedAccess();
    List<String> recovered = new ArrayList<>();

    for (MemberAccess ma : blockedRecords) {
      Member member = ma.getMember();
      Long memberId = member.getId();

      // Case 1: member has their own ACTIVE individual subscription
      Subscription activeSub = subscriptionRepository
          .findByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE);

      if (activeSub != null) {
        updateMemberAccess(memberId, activeSub.getGraceEndDate(),
            AccessStatus.ALLOWED,
            "Access restored - incorrectly blocked by scheduler");
        recovered.add(member.getMemberCode() + " (" + member.getFirstName() + ")");
        log.info("Restored access for member {} ({})", memberId, member.getMemberCode());
        continue;
      }

      // Case 2: member belongs to a family that has an ACTIVE family subscription
      if (member.getFamily() != null) {
        Optional<Subscription> familyActiveSub = subscriptionRepository
            .findByFamilyIdAndStatus(member.getFamily().getId(), SubscriptionStatus.ACTIVE);

        if (familyActiveSub.isPresent()) {
          updateMemberAccess(memberId, familyActiveSub.get().getGraceEndDate(),
              AccessStatus.ALLOWED,
              "Access restored - family subscription incorrectly blocked by scheduler");
          recovered.add(member.getMemberCode() + " (" + member.getFirstName() + ") [family]");
          log.info("Restored access for family member {} ({}) under family {}",
              memberId, member.getMemberCode(), member.getFamily().getId());
        }
      }
    }

    log.info("Recovery complete. Restored access for {} member(s).", recovered.size());
    return recovered;
  }

}
