package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.sercives.MemberService;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

//     register a member
    private final MemberRepository memberRepository;
    @Override
    public void registerMember(Member member) {
        memberRepository.save(member);
    }
}
