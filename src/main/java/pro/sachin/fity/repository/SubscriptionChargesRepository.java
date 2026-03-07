package pro.sachin.fity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sachin.fity.model.SubscriptionCharges;

@Repository
    public interface SubscriptionChargesRepository extends JpaRepository<SubscriptionCharges, Long> {
        
        Optional<SubscriptionCharges> findBySubscriptionId(Long subscriptionId);
    }
