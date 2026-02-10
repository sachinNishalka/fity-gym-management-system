package pro.sachin.fity.sercives.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.sachin.fity.dto.PaymentDTO;
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

}
