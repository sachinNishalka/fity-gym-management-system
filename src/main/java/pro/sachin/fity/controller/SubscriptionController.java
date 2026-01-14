package pro.sachin.fity.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Subscription;
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
    ResponseEntity<Subscription> saveSubscription(@RequestBody SubscriptionDTO subscriptionDTO ){

        final Subscription subscription = new Subscription();

//        checking for member id
            if(subscriptionDTO.getMemberId() != null){
                Member member = memberRepository.findById(subscriptionDTO.getMemberId()).orElseThrow(()->new EntityNotFoundException("Member is not found"));
                subscription.setMember(member);
            }
//        checking for family id



//        checking for plan id



    }
}
