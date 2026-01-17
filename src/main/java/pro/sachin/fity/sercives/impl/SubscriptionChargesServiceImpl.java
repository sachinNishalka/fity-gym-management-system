package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.sachin.fity.model.SubscriptionCharges;
import pro.sachin.fity.repository.SubscriptionChargesRepository;
import pro.sachin.fity.sercives.SubscriptionChargesService;

@RequiredArgsConstructor
@Service
public class SubscriptionChargesServiceImpl implements SubscriptionChargesService {

    private final SubscriptionChargesRepository subscriptionChargesRepository;

    @Override
    public void saveSubscriptionCharges(SubscriptionCharges subscriptionCharges) {
        subscriptionChargesRepository.save(subscriptionCharges);
    }
}
