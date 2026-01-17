package pro.sachin.fity.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionStatus;
import pro.sachin.fity.repository.FamilyRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.repository.PlanRepository;
import pro.sachin.fity.sercives.SubscriptionService;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    private final MemberRepository memberRepository;
    private final FamilyRepository familyRepository;
    private final PlanRepository planRepository;

    @PostMapping("/save")
    ResponseEntity<Subscription> saveSubscription(@RequestBody SubscriptionDTO subscriptionDTO) {

        final Subscription subscription = new Subscription();

        // checking for member id
        if (subscriptionDTO.getMemberId() != null) {
            Member member = memberRepository.findById(subscriptionDTO.getMemberId())
                    .orElseThrow(() -> new EntityNotFoundException("Member is not found"));
            subscription.setMember(member);
        }
        // checking for family id

        if (subscriptionDTO.getFamilyId() != null) {
            Family family = familyRepository.findById(subscriptionDTO.getFamilyId())
                    .orElseThrow(() -> new EntityNotFoundException("Family is not found"));
            subscription.setFamily(family);
        }

        // checking for plan id

        if (subscriptionDTO.getPlanId() != null) {
            Plan plan = planRepository.findById(subscriptionDTO.getPlanId())
                    .orElseThrow(() -> new EntityNotFoundException("Plan is not found"));
            subscription.setPlan(plan);
        }

        subscription.setStartDate(subscriptionDTO.getStartDate());
        subscription.setEndDate(subscriptionDTO.getEndDate());
        subscription.setDueDate(subscriptionDTO.getDueDate());
        subscription.setGraceEndDate(subscriptionDTO.getGraceEndDate());
        subscription.setStatus(SubscriptionStatus.valueOf(subscriptionDTO.getStatus()));

        subscriptionService.saveSubscription(subscription);

        return ResponseEntity.ok(subscription);
    }
}
