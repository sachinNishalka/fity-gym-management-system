package pro.sachin.fity.sercives;

import pro.sachin.fity.model.Subscription;

public interface NotificationQueueService {
    
    public void scheduleRenewalReminder(Subscription subscription);

    public void scheduleGraceWarning(Subscription subscription);

    public void scheduleBlockedNotice(Subscription subscription);

    public void cancelPendingNotifications(Long subscriptionId);

    
}
