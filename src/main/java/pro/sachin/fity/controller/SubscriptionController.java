package pro.sachin.fity.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pro.sachin.fity.dto.MemberDetailsDTO;
import pro.sachin.fity.dto.RenewalRequestDTO;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.mapper.SubscriptionMapper;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionStatus;
import pro.sachin.fity.repository.FamilyRepository;
import pro.sachin.fity.repository.MemberRepository;
import pro.sachin.fity.repository.PlanRepository;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.SubscriptionService;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/subscription")
@CrossOrigin
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // added for testing

    private final SubscriptionRepository subscriptionRepository;

    private final SubscriptionMapper subscriptionMapper;

    @PostMapping("/subscribe")
    ResponseEntity<SubscriptionDTO> subscribe(
            @RequestBody SubscriptionDTO subscriptionDTO) {
        subscriptionService.saveSubscription(subscriptionDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionDTO);
    }

    // TODO: BETTER APPROACH - Create new endpoint: POST /api/v1/subscription/enroll
    // TODO: Request should only contain:
    // - memberId OR familyId (not both)
    // - planId
    // - startDate (optional, default to today)
    // - discountAmount (optional, default to 0)
    // - initialPaymentAmount (optional, if paying immediately)
    // TODO: Service layer should:
    // 1. Validate member/family and plan exist
    // 2. Validate no overlapping active subscriptions
    // 3. Calculate all dates automatically
    // 4. Create subscription with status = ACTIVE
    // 5. Create subscription_charges with calculated amounts
    // 6. If initialPayment > 0, create payment record
    // 7. Update member_access based on payment status
    // 8. Schedule renewal reminder notification
    // 9. Return complete enrollment summary

    // TODO: IMPLEMENT - Get subscription by ID
    // TODO: GET /api/v1/subscription/{id}
    // TODO: Return subscription with charges, payments, balance due

    // TODO: IMPLEMENT - Get active subscription for member
    // TODO: GET /api/v1/subscription/member/{memberId}/active
    // TODO: Return current active subscription or null

    // TODO: IMPLEMENT - Get subscription history for member
    // TODO: GET /api/v1/subscription/member/{memberId}/history
    // TODO: Return all past and present subscriptions

    // TODO: IMPLEMENT - Renew subscription
    // TODO: POST /api/v1/subscription/{id}/renew
    // TODO: Create new subscription based on old one
    // TODO: Start date = old subscription end date + 1 day
    // TODO: Same plan (or allow plan change)

    // TODO: IMPLEMENT - Cancel subscription
    // TODO: POST /api/v1/subscription/{id}/cancel
    // TODO: Set status = ENDED
    // TODO: Update member_access = BLOCKED
    // TODO: Handle prorated refunds if applicable

    // TODO: IMPLEMENT - Get subscriptions by status
    // TODO: GET /api/v1/subscription/status/{status}
    // TODO: Return all ACTIVE, IN_GRACE, BLOCKED, or ENDED subscriptions
    // TODO: Use for monitoring dashboard

    // TODO: IMPLEMENT - Get subscriptions expiring soon
    // TODO: GET /api/v1/subscription/expiring?days=7
    // TODO: Return subscriptions ending within X days
    // TODO: Use for proactive renewal calls

    // TODO: IMPLEMENT - Get subscriptions in grace period
    // TODO: GET /api/v1/subscription/in-grace
    // TODO: SQL: WHERE current_date > end_date AND current_date <= grace_end_date
    // TODO: Use for payment follow-up

    // TODO: IMPLEMENT - Get subscriptions with balance due
    // TODO: GET /api/v1/subscription/balance-due
    // TODO: Join with charges and payments to calculate balance
    // TODO: Return subscriptions where payments < net_amount

    // TODO: VALIDATION - Add @Valid annotation
    // TODO: ERROR HANDLING - Use @ControllerAdvice for global exception handling
    // TODO: SECURITY - Add authentication, track createdByUserId

    @PostMapping("/renew")
    public ResponseEntity<Subscription> renewSubscription(
            @RequestBody RenewalRequestDTO renewalRequestDTO) {
        Subscription subscription = subscriptionService.createRenewalSubscription(renewalRequestDTO);
        return new ResponseEntity<Subscription>(subscription, HttpStatus.OK);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<SubscriptionDTO>> getPendingSubscriptions() {
        List<SubscriptionDTO> pendingSubscriptions = new ArrayList<>();

        List<Subscription> subscriptions = subscriptionService.getPendingSubscriptions();

        for (Subscription subscription : subscriptions) {
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
            subscriptionDTO.setId(subscription.getId());

            if (subscription.getMember() != null) {
                subscriptionDTO.setMemberId(subscription.getMember().getId());

                subscriptionDTO.setMemberName(
                        subscription.getMember().getFirstName() + " " + subscription.getMember().getLastName());
            }

            if (subscription.getFamily() != null) {
                subscriptionDTO.setFamilyId(subscription.getFamily().getId());
                subscriptionDTO.setFamilyName(subscription.getFamily().getFamilyName());
            }

            subscriptionDTO.setPlanId(subscription.getPlan().getId());
            subscriptionDTO.setPlanName(subscription.getPlan().getName());

            subscriptionDTO.setStartDate(subscription.getStartDate());
            subscriptionDTO.setEndDate(subscription.getEndDate());
            subscriptionDTO.setDueDate(subscription.getDueDate());
            subscriptionDTO.setGraceEndDate(subscription.getGraceEndDate());
            subscriptionDTO.setStatus(subscription.getStatus().name());
            subscriptionDTO.setDiscountAmount(subscription.getSubscriptionCharges().getDiscountAmount());
            subscriptionDTO.setMemberCode(subscription.getMember().getMemberCode());
            pendingSubscriptions.add(subscriptionDTO);
        }

        return new ResponseEntity<List<SubscriptionDTO>>(pendingSubscriptions, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SubscriptionDTO>> getAllSubscriptions() {
        List<SubscriptionDTO> allSubscriptions = new ArrayList<>();

        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();

        for (Subscription subscription : subscriptions) {
            SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
            subscriptionDTO.setId(subscription.getId());

            if (subscription.getMember() != null) {
                subscriptionDTO.setMemberId(subscription.getMember().getId());

                subscriptionDTO.setMemberName(
                        subscription.getMember().getFirstName() + " " + subscription.getMember().getLastName());
            }

            if (subscription.getFamily() != null) {
                subscriptionDTO.setFamilyId(subscription.getFamily().getId());
                subscriptionDTO.setFamilyName(subscription.getFamily().getFamilyName());
            }

            subscriptionDTO.setPlanId(subscription.getPlan().getId());
            subscriptionDTO.setPlanName(subscription.getPlan().getName());

            subscriptionDTO.setStartDate(subscription.getStartDate());
            subscriptionDTO.setEndDate(subscription.getEndDate());
            subscriptionDTO.setDueDate(subscription.getDueDate());
            subscriptionDTO.setGraceEndDate(subscription.getGraceEndDate());
            subscriptionDTO.setStatus(subscription.getStatus().name());
            subscriptionDTO.setMemberCode(subscription.getMember().getMemberCode());
            if (subscription.getSubscriptionCharges() != null) {
                subscriptionDTO.setDiscountAmount(subscription.getSubscriptionCharges().getDiscountAmount());
            } else {
                log.error("Subscription charges not found for subscription: " + subscription.getId());
            }
            allSubscriptions.add(subscriptionDTO);
        }

        return new ResponseEntity<List<SubscriptionDTO>>(allSubscriptions, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{subscriptionId}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable("subscriptionId") Long subscriptionId) {
        subscriptionService.deleteSubscription(subscriptionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<MemberDetailsDTO> getSubscriptionByMemberId(@PathVariable("memberId") Long memberId) {
        MemberDetailsDTO memberDetailsDTO = subscriptionService.getSubscriptionByMemberId(memberId);
        return new ResponseEntity<MemberDetailsDTO>(memberDetailsDTO, HttpStatus.OK);
    }

    @GetMapping("/renewal")
    public ResponseEntity<List<SubscriptionDTO>> getRenewalSubscriptionList() {
        List<SubscriptionDTO> renewalSubscriptionsList = subscriptionService.getRenewalSubscriptionsList();
        return new ResponseEntity<List<SubscriptionDTO>>(renewalSubscriptionsList, HttpStatus.OK);
    }

    @GetMapping("/test")
    public List<SubscriptionDTO> getMethodName() {

        List<SubscriptionDTO> subscriptionDTOList = new ArrayList<>();

        List<SubscriptionStatus> subsriptionList = new ArrayList<>();

        subsriptionList.add(SubscriptionStatus.ACTIVE);
        subsriptionList.add(SubscriptionStatus.DUE);

        List<Subscription> list = subscriptionRepository.findByEndDateBeforeAndStatusIn(LocalDate.now(),
                subsriptionList);

        for (Subscription subscription : list) {
            SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);
            subscriptionDTOList.add(subscriptionDTO);
        }

        return subscriptionDTOList;
    }

    @PostMapping("/renewal/custom")
    public ResponseEntity<Subscription> createRenewalSubscriptionCustomDate(
            @RequestBody RenewalRequestDTO renewalRequestDTO) {
        Subscription subscription = subscriptionService.createRenewalSubscriptionCustomDate(renewalRequestDTO);
        return new ResponseEntity<Subscription>(subscription, HttpStatus.OK);
    }

}
