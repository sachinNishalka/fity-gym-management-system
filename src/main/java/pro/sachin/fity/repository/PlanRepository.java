package pro.sachin.fity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sachin.fity.model.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

}
