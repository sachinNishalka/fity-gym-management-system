package pro.sachin.fity.sercives.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.sachin.fity.dto.GraceExtensionDTO;
import pro.sachin.fity.dto.GraceExtensonResponseDTO;
import pro.sachin.fity.mapper.GraceExtensionResponseMapper;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.GraceExtension;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionStatus;
import pro.sachin.fity.repository.GraceExtensionRepository;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.GraceExtensionService;
import pro.sachin.fity.sercives.MemberAccessService;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraceExtensionServiceImpl implements GraceExtensionService {

    private final SubscriptionRepository subscriptionRepository;
    private final GraceExtensionRepository graceExtensionRepository;
    private final MemberAccessService memberAccessService;
    private final GraceExtensionResponseMapper graceExtensionResponseMapper;

    @Override
    @Transactional
    public ResponseEntity<?> extendGracePeriod(Long subscriptionId, LocalDate newGraceEndDate, String reason,
            Long extendedByUserId) {

        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                () -> new EntityNotFoundException("Subscription is not found for grace period extenstion!"));

        if (subscription.getStatus() == SubscriptionStatus.ENDED) {
            throw new IllegalStateException("Grace period cannot be granted to an ended subscription.");
        }

        LocalDate oldGraceEndDate = subscription.getGraceEndDate();
        if (oldGraceEndDate == null) {
            oldGraceEndDate = LocalDate.now();
        }

        // checking extension date is after tje previous date

        if (newGraceEndDate == null || !newGraceEndDate.isAfter(oldGraceEndDate)) {
            throw new IllegalArgumentException("New grace date must be after current grace date!");
        }

        long extensionDays = ChronoUnit.DAYS.between(oldGraceEndDate, newGraceEndDate);

        // checking extension days
        if (extensionDays > 7) {
            throw new IllegalArgumentException("Cannot extend by more than 7 days");
        }

        // checking the extension limits
        long extensionCount = graceExtensionRepository.countBySubscriptionId(subscriptionId);

        if (extensionCount >= 2) {
            throw new IllegalStateException("Maximum 2 extensions per subscription reached");
        }

        // if none of above happens then create the grace extension subscription

        GraceExtension extension = new GraceExtension();

        extension.setSubscription(subscription);

        extension.setOldGraceEndDate(oldGraceEndDate);
        extension.setNewGraceEndDate(newGraceEndDate);
        extension.setReason(reason);

        GraceExtension savedExtension = graceExtensionRepository.save(extension);

        // change the grace end date for the subscription

        subscription.setGraceEndDate(savedExtension.getNewGraceEndDate());
        subscription.setStatus(SubscriptionStatus.IN_GRACE);
        subscriptionRepository.save(subscription);

        // change the door access (extending door access)

        if (subscription.getMember() != null) {
            memberAccessService.updateMemberAccess(savedExtension.getSubscription().getMember().getId(),
                    savedExtension.getNewGraceEndDate(), AccessStatus.ALLOWED, reason);
        } else if (subscription.getFamily() != null) {
            // here goes the access extension for family members
            log.info("Family subscription grace extended");
        }

        log.info("Grace extension for subscription {} by days {}", subscriptionId, extensionDays);

        return new ResponseEntity<>("Grace period extended successfully", HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> extend(GraceExtensionDTO graceExtensionDTO) {

        if (graceExtensionDTO.getSubscriptionId() != null) {
            return extendGracePeriod(graceExtensionDTO.getSubscriptionId(), graceExtensionDTO.getNewGraceEndDate(),
                    graceExtensionDTO.getReason(), null);
        }

        return null;

    }

    @Override
    public List<GraceExtensonResponseDTO> extendedList() {

        List<GraceExtensonResponseDTO> list = new ArrayList<>();
        List<GraceExtension> listGraceExtended = graceExtensionRepository.findAll();

        for (GraceExtension graceExtend : listGraceExtended) {

            Subscription subscription = graceExtend.getSubscription();
            Member member = subscription.getMember();

            list.add(graceExtensionResponseMapper.toDto(graceExtend, subscription, member));

        }

        return list;

    }

}
