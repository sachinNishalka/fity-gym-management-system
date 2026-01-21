package pro.sachin.fity.sercives.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.sachin.fity.dto.GraceExtensionDTO;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.GraceExtension;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.model.SubscriptionStatus;
import pro.sachin.fity.model.User;
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

    

    @Override
    public GraceExtension extendGracePeriod(Long subscriptionId, LocalDate newGraceEndDate, String reason,
            Long extendedByUserId) {
            
                Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(()-> new EntityNotFoundException("Subscription is not found for grace period extenstion!"));

                if(subscription.getStatus() != SubscriptionStatus.IN_GRACE){
                    throw new IllegalStateException("can only extend grace for IN_GRACE subscriptions");
                }

                // for now we are manually setting this, i m planning ot implement it later
                User extendedBy = new User();
                extendedBy.setName("sachin");

                LocalDate oldGraceEndDate = subscription.getGraceEndDate();

                // checking extension date is after tje previous date

                if(!newGraceEndDate.isAfter(oldGraceEndDate)){
                    throw new IllegalArgumentException("New grace date must be after current grace date!");
                }

                long extensionDays = ChronoUnit.DAYS.between(oldGraceEndDate, newGraceEndDate);
                
                // checking extension days 
                if(extensionDays>7){
                    throw new IllegalArgumentException("Cannot extend by more than 7 days");
                }

                // checking the extension limits 
                long extensionCount = graceExtensionRepository.countBySubscriptionId(subscriptionId);

                if(extensionCount >=3){
                    throw new IllegalStateException("Maximum 3 extensions per subscription reached");
                }

                // if none of above happens then create the grace extension subscription

                GraceExtension extension = new GraceExtension();

                extension.setSubscription(subscription);
                
                extension.setExtendedByUser(extendedBy);
                extension.setOldGraceEndDate(oldGraceEndDate);
                extension.setNewGraceEndDate(newGraceEndDate);
                extension.setReason(reason);

               GraceExtension savedExtension = graceExtensionRepository.save(extension);

            //    change the grace end date for the subscription 

                subscription.setGraceEndDate(savedExtension.getNewGraceEndDate());
                subscriptionRepository.save(subscription);

            // change the door access (extending door access)
            
                if(subscription.getMember() != null){
                    memberAccessService.updateMemberAccess(savedExtension.getSubscription().getMember().getId(), savedExtension.getNewGraceEndDate(), AccessStatus.ALLOWED, reason);
                } else if(subscription.getFamily()!=null){
                    // here goes the access extension for family members
                    log.info("Family subscription grace extended");
                }

                log.info("Grace extension for subscription {} by days {}", subscriptionId, extensionDays);
                
                return savedExtension;
    }



    @Override
    public GraceExtension extend(GraceExtensionDTO graceExtensionDTO) {

        if(graceExtensionDTO.getSubscriptionId() != null){
            return extendGracePeriod(graceExtensionDTO.getSubscriptionId(), graceExtensionDTO.getNewGraceEndDate(), graceExtensionDTO.getReason(), null);
        }

        return null;

    }
    

}
