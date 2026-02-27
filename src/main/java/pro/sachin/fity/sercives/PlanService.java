package pro.sachin.fity.sercives;

import java.util.List;

import pro.sachin.fity.dto.PlanDTO;
import pro.sachin.fity.model.Plan;

public interface PlanService {

    void createPlan(PlanDTO planDTO);

    List<Plan> getAllPlans();

    Plan getPlanById(Long id);

    Plan updatePlan(Long id, PlanDTO planDTO);

    void deletePlan(Long id);


}
