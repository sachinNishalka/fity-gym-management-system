package pro.sachin.fity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pro.sachin.fity.model.Payments;

public interface PyamentRepository extends JpaRepository<Payments, Long> {
     
    List<Payments> findBySubscriptionId(Long subscriptionId);
}
