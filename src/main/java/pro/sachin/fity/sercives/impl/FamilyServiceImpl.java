package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import pro.sachin.fity.dto.FamilyDTO;
import pro.sachin.fity.dto.FamilyResponseDTO;
import pro.sachin.fity.dto.MemberSummeryDTO;
import pro.sachin.fity.dto.PlanDTO;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.mapper.SubscriptionMapper;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.model.PlanType;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.repository.FamilyRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.repository.PlanRepository;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.FamilyService;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final PlanRepository planRepository;

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

    @Override
    public FamilyResponseDTO addMemberToFamily(Long familyId, Long memberId) {

        Family family = familyRepository.findById(familyId).orElseThrow(() -> new EntityNotFoundException(
                "Requested family not found (add member to family method, family service implimentation)"));

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException(
                "Requested member not found (add member to family method, family service implimentation)"));

        // Check if the member is already part of another family
        if (member.getFamily() != null && !member.getFamily().getId().equals(familyId)) {
            throw new IllegalArgumentException("Member is already part of another family.");
        }

        // Add the member to the family
        member.setFamily(family);
        memberRepository.save(member);

        return getFamilyById(familyId);
    }

    @Override
    public void removeMemberFromFamily(Long familyId, Long memberId) {

        Family family = familyRepository.findById(familyId).orElseThrow(() -> new EntityNotFoundException(
                "Requested family not found (remove member from family method, family service implimentation)"));

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException(
                "Requested member not found (remove member from family method, family service implimentation)"));

        // Check if the member belongs to the specified family
        if (member.getFamily() == null || !member.getFamily().getId().equals(familyId)) {
            throw new IllegalArgumentException("Member does not belong to the specified family.");
        }

        // Remove the member from the family
        member.setFamily(null);
        memberRepository.save(member);

    }

    @Override
    public FamilyResponseDTO updateFamilyName(Long familyId, String familyName) {
        Family family = familyRepository.findById(familyId).orElseThrow(() -> new EntityNotFoundException(
                "Requested family not found (update family name method, family service implimentation)"));

        // Update the family name
        family.setFamilyName(familyName);
        familyRepository.save(family);

        return getFamilyById(familyId);
    }

    @Override
    public void deleteFamily(Long familyId) {

        Family family = familyRepository.findById(familyId).orElseThrow(() -> new EntityNotFoundException(
                "Requested family not found (delete family method, family service implimentation)"));

        // Check if the family has active subscriptions
        if (!family.getMembers().isEmpty()) {
            throw new IllegalStateException("Cannot delete family with active members.");
        }

        // Remove all member associations
        for (Member member : family.getMembers()) {
            member.setFamily(null);
            memberRepository.save(member);
        }

        // Delete the family
        familyRepository.delete(family);
    }

    @Override
    public SubscriptionDTO getAllSubscriptionsForFamily(Long familyId) {
        Subscription subscriptions = subscriptionRepository.findByFamilyId(familyId);

        if (subscriptions == null) {
            throw new EntityNotFoundException(
                    "No subscriptions found for the specified family (get all subscriptions for family method, family service implimentation)");
        }

        SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscriptions);

        return subscriptionDTO;
    }

    @Override
    public List<PlanDTO> getEligiblePlansForFamily(Long familyId) {
        List<PlanDTO> eligiblePlans = planRepository.findByPlanType(PlanType.FAMILY).stream()
                .map(plan -> {
                    PlanDTO planDTO = new PlanDTO();
                    planDTO.setId(plan.getId());
                    planDTO.setName(plan.getName());
                    planDTO.setPlanType(plan.getPlanType());
                    planDTO.setMaximumFamilyMembers(plan.getMaximumFamilyMembers());
                    return planDTO;
                }).toList();
        return eligiblePlans;
    }
}
