package pro.sachin.fity.sercives.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.sachin.fity.model.NotificationChannel;
import pro.sachin.fity.model.NotificationQueue;
import pro.sachin.fity.model.NotificationStatus;
import pro.sachin.fity.model.NotificationType;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.repository.NotificationQueueRepository;
import pro.sachin.fity.sercives.NotificationQueueService;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationQueueServiceImpl implements NotificationQueueService {

    private final NotificationQueueRepository notificationQueueRepository;

    @Override
    public void scheduleRenewalReminder(Subscription subscription) {
        // check the notification is already scheduled 
        boolean isScheduled = notificationQueueRepository.existsBySubscriptionIdAndNotificationTypeAndStatus(subscription.getId(), NotificationType.RENEWAL_REMINDER, NotificationStatus.PENDING);


        if(isScheduled) {
            return;
        }

        NotificationQueue notificationQueue = new NotificationQueue();

        notificationQueue.setMember(subscription.getMember());
        notificationQueue.setSubscription(subscription);
        notificationQueue.setNotificationType(NotificationType.RENEWAL_REMINDER);

        // here this is scheduled for a time, but at the initial stage we are not goind to send any messages, instead
        // we are going to sotre in the database for now and then we will show it in the time of entering the member to the gym

        // the below code is for future implementation 
        LocalDateTime scheduledFor = subscription.getDueDate().atTime(10, 0);
        if(LocalDateTime.now().isAfter(scheduledFor)) {
            scheduledFor = scheduledFor.plusDays(1);
        }
        notificationQueue.setScheduledFor(scheduledFor);
        notificationQueue.setStatus(NotificationStatus.PENDING);
        notificationQueue.setChannel(NotificationChannel.SMS);
        notificationQueueRepository.save(notificationQueue);
        log.info("Scheduled renewal reminder for member {}", subscription.getMember().getId());
    
    }

    @Override
    public void scheduleGraceWarning(Subscription subscription) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'scheduleGraceWarning'");
    }

    @Override
    public void scheduleBlockedNotice(Subscription subscription) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'scheduleBlockedNotice'");
    }

    @Override
    public void cancelPendingNotifications(Long subscriptionId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelPendingNotifications'");
    }
    
}
