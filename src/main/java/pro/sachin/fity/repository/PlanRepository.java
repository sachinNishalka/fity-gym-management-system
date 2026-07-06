package pro.sachin.fity.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.model.PlanType;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Collection<Plan> findByPlanType(PlanType family);

}
