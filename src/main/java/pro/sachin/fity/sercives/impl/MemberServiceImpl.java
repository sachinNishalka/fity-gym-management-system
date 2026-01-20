package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pro.sachin.fity.dto.MemberDTO;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.MemberStatus;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.sercives.MemberService;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

//     register a member
    private final MemberRepository memberRepository;
    @Override
    public void registerMember(MemberDTO memberDTO) {
        Member member = new Member();
        member.setFirstName(memberDTO.getFirstName());
        member.setLastName(memberDTO.getLastName());
        member.setDateOfBirth(memberDTO.getDateOfBirth());
        member.setPhoneNumber(memberDTO.getPhoneNumber());
        member.setEmail(memberDTO.getEmail());
        member.setStatus(MemberStatus.ACTIVE);
        memberRepository.save(member);
    }
}
