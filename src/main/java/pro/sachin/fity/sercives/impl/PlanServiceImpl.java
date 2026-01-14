package pro.sachin.fity.sercives.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.repository.PlanRepository;
import pro.sachin.fity.sercives.PlanService;

@RequiredArgsConstructor
@Service
public class PlanServiceImpl implements PlanService {
    private final PlanRepository planRepository;
    @Override
    public void createPlan(Plan plan) {
        planRepository.save(plan);
    }
}
