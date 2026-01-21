package pro.sachin.fity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pro.sachin.fity.model.GraceExtension;

public interface GraceExtensionRepository extends JpaRepository<GraceExtension, Long> {

    long countBySubscriptionId(long subscriptionId);
}
