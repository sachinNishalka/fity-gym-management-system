package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import pro.sachin.fity.dto.FamilyDTO;
import pro.sachin.fity.dto.FamilyResponseDTO;
import pro.sachin.fity.dto.MemberSummeryDTO;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.repository.FamilyRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.sercives.FamilyService;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public Family createFamily(FamilyDTO familyDTO) {
        Family family = new Family();
        family.setFamilyName(familyDTO.getFamilyName());
        Family savedFamily = familyRepository.save(family);

        // assign members

        for (Long memberId : familyDTO.getMemberIds()) {
            // finding the member infromation
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException(
                    "The member you are looking for is not registered yet (error occured while creating family and assingning)"));
            member.setFamily(savedFamily);
            memberRepository.save(member);
        }

        return familyRepository.findById(savedFamily.getId()).orElse(savedFamily);
    }

    @Transactional
    @Override
    public FamilyResponseDTO getFamilyById(Long id) {
        Family family = familyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                "Requested family not found (get family by id method, family service implimentation)"));

        FamilyResponseDTO familyResponseDTO = new FamilyResponseDTO();
        familyResponseDTO.setId(family.getId());
        familyResponseDTO.setFamilyName(family.getFamilyName());
        familyResponseDTO.setCreatedAt(family.getCreatedAt());

        // converting members to the summery dtos

        List<MemberSummeryDTO> memberSummeries = family.getMembers().stream().map(member -> {
            MemberSummeryDTO memberSummeryDTO = new MemberSummeryDTO();
            memberSummeryDTO.setId(member.getId());
            memberSummeryDTO.setFirstName(member.getFirstName());
            memberSummeryDTO.setLastName(member.getLastName());
            memberSummeryDTO.setPhoneNumber(member.getPhoneNumber());
            memberSummeryDTO.setEmail(member.getEmail());
            memberSummeryDTO.setStatus(member.getStatus());
            memberSummeryDTO.setJoinedDate(member.getJoinedDate());
            return memberSummeryDTO;
        }).toList();

        familyResponseDTO.setMembers(memberSummeries);
        return familyResponseDTO;
    }
}
