package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pro.sachin.fity.dto.MemberDTO;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.MemberStatus;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.sercives.DeviceCommandService;
import pro.sachin.fity.sercives.MemberService;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    // register a member
    private final MemberRepository memberRepository;
    private final DeviceCommandService deviceCommandService;

    @Override
    public void registerMember(MemberDTO memberDTO) {
        Member member = new Member();
        // member.setFirstName(memberDTO.getFirstName());
        // member.setLastName(memberDTO.getLastName());
        // member.setDateOfBirth(memberDTO.getDateOfBirth());
        // member.setPhoneNumber(memberDTO.getPhoneNumber());
        // member.setEmail(memberDTO.getEmail());
        // member.setStatus(MemberStatus.ACTIVE);
        // member.setGender(memberDTO.getGender());
        // member.setMemberCode(memberDTO.getMemberCode());

        member.setFirstName(memberDTO.getFirstName());

        member.setLastName(memberDTO.getLastName());

        member.setDateOfBirth(memberDTO.getDateOfBirth());

        member.setPhoneNumber(memberDTO.getPhoneNumber());

        member.setEmail(memberDTO.getEmail());

        member.setJoinedDate(memberDTO.getJoinedDate());

        member.setStatus(memberDTO.getStatus());

        member.setGender(memberDTO.getGender());

        member.setMemberCode(memberDTO.getMemberCode());

        member.setAddress(memberDTO.getAddress());

        member.setHeight(memberDTO.getHeight());

        member.setWeight(memberDTO.getWeight());

        member.setIdNumber(memberDTO.getIdNumber());

        member.setEmergencyNumber(memberDTO.getEmergencyNumber());

        member.setFacebookName(memberDTO.getFacebookName());

        member.setBodyBuilding(memberDTO.getBodyBuilding());

        member.setFatBurning(memberDTO.getFatBurning());

        member.setPhysicalFitness(memberDTO.getPhysicalFitness());

        member.setSportsSkills(memberDTO.getSportsSkills());

        member.setBodyShape(memberDTO.getBodyShape());

        member.setOtherService(memberDTO.getOtherService());

        member.setCholesterol(memberDTO.getCholesterol());

        member.setBloodPressure(memberDTO.getBloodPressure());

        member.setDiabetes(memberDTO.getDiabetes());

        member.setHeartProblem(memberDTO.getHeartProblem());

        member.setSurgery(memberDTO.getSurgery());

        member.setFractures(memberDTO.getFractures());

        member.setKidneyLiver(memberDTO.getKidneyLiver());

        member.setOtherDisease(memberDTO.getOtherDisease());

        member.setCurrentTreatment(memberDTO.getCurrentTreatment());

        member.setStatus(MemberStatus.ACTIVE);

        memberRepository.save(member);

        deviceCommandService.queueMemberRegistration(member);
    }

    @Override
    public List<Member> getAllMembers() {

        List<Member> members = memberRepository.findAll();
        return members;

    }

    @Override
    public Member getMemberById(Long id) {
        
        Member member = memberRepository.findById(id).orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
        return member;
    }

}
