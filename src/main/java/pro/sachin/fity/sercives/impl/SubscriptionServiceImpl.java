package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.SubscriptionService;

@RequiredArgsConstructor
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public void saveSubscription(Subscription subscription) {
        subscriptionRepository.save(subscription);
    }
}
