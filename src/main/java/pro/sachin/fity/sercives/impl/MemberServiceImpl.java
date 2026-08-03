package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pro.sachin.fity.dto.MemberDTO;
import pro.sachin.fity.exception.DeviceCommandExceptions.DeviceCommandException;
import pro.sachin.fity.exception.MemberExceptions.MemberAccessCreationException;
import pro.sachin.fity.exception.MemberExceptions.MemberNotFoundException;
import pro.sachin.fity.exception.MemberExceptions.MemberRegistrationException;
import pro.sachin.fity.mapper.MemberMapper;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.MemberAccess;
import pro.sachin.fity.model.MemberStatus;
import pro.sachin.fity.repository.MemberAccessRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.sercives.DeviceCommandService;
import pro.sachin.fity.sercives.MemberService;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberAccessRepository memberAccessRepository;
    private final DeviceCommandService deviceCommandService;
    private final MemberMapper memberMapper;

    @Override
    @Transactional
    public void registerMember(MemberDTO memberDTO) {
        try {
            // Use mapper to convert DTO to entity
            Member member = memberMapper.toEntity(memberDTO);
            
            // Override critical default values
            member.setStatus(MemberStatus.ACTIVE);
            member.setJoinedDate(LocalDateTime.now());
            
            // Save member
            member = memberRepository.save(member);
            log.info("Member registered successfully with ID: {} and code: {}", member.getId(), member.getMemberCode());
            
            // Create MemberAccess record with BLOCKED status
            MemberAccess memberAccess = new MemberAccess();
            memberAccess.setMember(member);
            memberAccess.setAccessStatus(AccessStatus.BLOCKED);
            memberAccess.setReason("New member registration - awaiting subscription");
            memberAccess.setAllowedUntil(null);
            
            memberAccessRepository.save(memberAccess);
            log.info("MemberAccess created with BLOCKED status for member ID: {}", member.getId());
            
            // Queue device command for fingerprint registration
            deviceCommandService.queueMemberRegistration(member);
            log.info("Device command queued for member registration: {}", member.getMemberCode());
            
        } catch (DeviceCommandException e) {
            log.error("Failed to queue device command for member registration", e);
            throw e; // Re-throw to trigger transaction rollback
        } catch (Exception e) {
            log.error("Failed to register member", e);
            throw new MemberRegistrationException("Failed to register member: " + e.getMessage());
        }
    }

    @Override
    public List<MemberDTO> getAllMembers() {

        List<Member> members = memberRepository.findAll();

        List<MemberDTO> memberDTOs = new ArrayList<>();

        for (Member member : members) {
            MemberDTO memberDTO = memberMapper.toDto(member);
            if (member.getMemberAccess() != null) {
                memberDTO.setMemberAccessStatus(member.getMemberAccess().getAccessStatus().toString());
            } else {
                memberDTO.setMemberAccessStatus("ACCESS_DENIED");
            }
            memberDTOs.add(memberDTO);
        }

        return memberDTOs;

    }

    @Override
    public MemberDTO getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + id));

        MemberDTO memberDTO = memberMapper.toDto(member);

        if (member.getMemberAccess() != null) {
            memberDTO.setMemberAccessStatus(member.getMemberAccess().getAccessStatus().toString());
        } else {
            memberDTO.setMemberAccessStatus("ACCESS_DENIED");
        }

        return memberDTO;
    }

    @Override
    public Member updateMember(Long memberId, MemberDTO memberDTO) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        Member updatedMember = memberMapper.updateMemberFromDto(memberDTO, member);

        return memberRepository.save(updatedMember);
    }

    @Override
    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }

}
