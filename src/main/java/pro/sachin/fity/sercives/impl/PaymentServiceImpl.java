package pro.sachin.fity.sercives.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.sachin.fity.dto.PartiallyPaidSubscriptionDTO;
import pro.sachin.fity.dto.PaymentDTO;
import pro.sachin.fity.dto.SubscriptionDTO;
import pro.sachin.fity.mapper.SubscriptionMapper;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.Family;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Payments;
import pro.sachin.fity.repository.PyamentRepository;
import pro.sachin.fity.repository.SubscriptionChargesRepository;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.MemberAccessService;
import pro.sachin.fity.sercives.PaymentService;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionCharges;
import pro.sachin.fity.model.SubscriptionStatus;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PyamentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionChargesRepository subscriptionChargesRepository;
    private final MemberAccessService memberAccessService;
    private final SubscriptionMapper  subscriptionMapper;

    @Override
    @Transactional
    public void savePayment(PaymentDTO paymentDTO) {

        final Payments payments = new Payments();

        if (paymentDTO.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.findById(paymentDTO.getSubscriptionId())
                    .orElseThrow(() -> new EntityNotFoundException("Subscription is not found"));
            payments.setSubscription(subscription);
        }

        payments.setAmount(paymentDTO.getAmount());
        payments.setReceiptNo(paymentDTO.getRecieptNo());
        payments.setNote(paymentDTO.getNote());

        Payments savedPayment = paymentRepository.save(payments);
        log.info("Payment saved : {} for subscription {}", payments.getAmount(), payments.getSubscription().getId());
        processPostPayment(savedPayment.getSubscription().getId());

        // member access update issue with family and single persons

    }

    private void processPostPayment(Long subscriptionId) {
        boolean isFullyPaid = checkIfSubscriptionIsFullyPaid(subscriptionId);
        if (isFullyPaid) {
            log.info("Subscription {} is fully paid", subscriptionId);
            activateSubscription(subscriptionId);

            Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                    () -> new EntityNotFoundException("Subscription is not found to update access records!"));

            if (subscription.getMember() != null) {

                memberAccessService.updateMemberAccess(subscription.getMember().getId(), subscription.getGraceEndDate(),
                        AccessStatus.ALLOWED, "Payment made and subcription restored!");
            } else if (subscription.getFamily() != null) {
                // This should be updated with family subscriptioin - we havent implemeted yet
                // TODO

                Family family = subscription.getFamily();

                for (Member member : family.getMembers()) {
                    memberAccessService.updateMemberAccess(member.getId(), subscription.getGraceEndDate(),
                            AccessStatus.ALLOWED, "Family payment recived");
                    log.info("Resotered access for {} in family {}", member.getFirstName(), family.getFamilyName());
                }

                log.info("Family Subscription fully paid!");
            }

        } else {
            log.info("Subscription {} is not fully paid, has outstanding balance", subscriptionId);
        }
    }

    private boolean checkIfSubscriptionIsFullyPaid(Long subscriptionId) {

        List<Payments> payments = paymentRepository.findBySubscriptionId(subscriptionId);

        BigDecimal totalPaid = payments.stream().map(payment -> payment.getAmount()).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        SubscriptionCharges subscriptionCharges = subscriptionChargesRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new IllegalStateException("Subscription charges not found"));

        BigDecimal netAmount = subscriptionCharges.getNetAmount();

        boolean isFullyPaid = totalPaid.compareTo(netAmount) >= 0;

        log.info("Subscription {}: Paid {}, Required {}, Fully Paid: {}", subscriptionId, totalPaid, netAmount,
                isFullyPaid);

        return isFullyPaid;
    }

    private void activateSubscription(Long subscriptionId) {

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalStateException("Subscription not found"));

        if (subscription.getStatus() == SubscriptionStatus.BLOCKED) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(subscription);
            log.info("Subscription {} reactivated", subscriptionId);
        }

        // acitvate the sibscription after the first payment
        if (subscription.getStatus() == SubscriptionStatus.PENDING) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(subscription);
            log.info("Subscription {} activated from PENDING (first payment)", subscriptionId);
        }
    }

    public List<PartiallyPaidSubscriptionDTO> getPartiallyPaidSubscriptions() {
        List<PartiallyPaidSubscriptionDTO> partiallyPaidSubscriptions = new ArrayList<>();

        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        for (Subscription subscription : allSubscriptions) {
            if (!checkIfSubscriptionIsFullyPaid(subscription.getId())) {

                // it needs the total amount
                BigDecimal totalAmount = subscriptionChargesRepository.findBySubscriptionId(subscription.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Subscription charges record not found for subscription"))
                        .getNetAmount();

                // it needs paid amout
                List<Payments> payments = paymentRepository.findBySubscriptionId(subscription.getId());

                BigDecimal paidAmount = BigDecimal.ZERO;

                for (Payments payment : payments) {
                    paidAmount = paidAmount.add(payment.getAmount());
                }

                // it needs balance amount
                BigDecimal balanceAmount = totalAmount.subtract(paidAmount);

                PartiallyPaidSubscriptionDTO dto = new PartiallyPaidSubscriptionDTO();
                dto.setSubscriptionId(subscription.getId());
                dto.setTotalAmount(totalAmount);
                dto.setPaid(paidAmount);
                dto.setBalance(balanceAmount);

                dto.setMemberFirstName(subscription.getMember().getFirstName());
                dto.setMemberLastName(subscription.getMember().getLastName());
                dto.setMemberId(subscription.getMember().getId());

                dto.setPlanId(subscription.getPlan().getId());
                dto.setPlanName(subscription.getPlan().getName());
                dto.setPlanType(subscription.getPlan().getPlanType().name());

                // subcriptio id
                partiallyPaidSubscriptions.add(dto);
            }
        }

        return partiallyPaidSubscriptions;
    }

    public List<Subscription> paymentsForToday() {
        List<Subscription> subscriptions = subscriptionRepository.findByEndDate(java.time.LocalDate.now());
        return subscriptions;
    }

    @Override
    public List<SubscriptionDTO> getMissingPayments() {

        List<SubscriptionDTO> subscriptionDTOs = new ArrayList<>();
        
        List<Subscription> subscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.IN_GRACE);

        for (Subscription subscription : subscriptions) {
            SubscriptionDTO subscriptionDTO = subscriptionMapper.toDto(subscription);
                subscriptionDTO.setId(subscription.getId());

                if (subscription.getMember() != null) {
                    subscriptionDTO.setMemberId(subscription.getMember().getId());
                    subscriptionDTO.setMemberName(subscription.getMember().getFirstName()+ " " + subscription.getMember().getLastName());
                }

                if (subscription.getFamily() != null) {
                    subscriptionDTO.setFamilyId(subscription.getFamily().getId());
                    subscriptionDTO.setFamilyName(subscription.getFamily().getFamilyName());
                }

                subscriptionDTO.setPlanId(subscription.getPlan().getId());
                subscriptionDTO.setPlanName(subscription.getPlan().getName());

                subscriptionDTOs.add(subscriptionDTO);
              
        }

        return subscriptionDTOs;
    }

}
