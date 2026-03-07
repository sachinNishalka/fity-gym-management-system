package pro.sachin.fity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pro.sachin.fity.model.NotificationQueue;
import pro.sachin.fity.model.NotificationStatus;
import pro.sachin.fity.model.NotificationType;

public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, Long> {
    boolean existsBySubscriptionIdAndNotificationTypeAndStatus(Long subscriptionId, NotificationType notificationType, NotificationStatus status);
}
