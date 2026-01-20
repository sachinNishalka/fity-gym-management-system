package pro.sachin.fity.sercives.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import pro.sachin.fity.dto.PlanDTO;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.repository.PlanRepository;
import pro.sachin.fity.sercives.PlanService;

@RequiredArgsConstructor
@Service
public class PlanServiceImpl implements PlanService {
    private final PlanRepository planRepository;
    @Override
    public void createPlan(PlanDTO planDTO) {
        Plan plan = new Plan();
        plan.setName(planDTO.getName());
        plan.setDurationDays(planDTO.getDurationDays());
        plan.setPrice(planDTO.getPrice());
        plan.setPlanType(planDTO.getPlanType());
        plan.setAgeMin(planDTO.getAgeMin());
        plan.setAgeMax(planDTO.getAgeMax());
        plan.setMaximumFamilyMembers(planDTO.getMaximumFamilyMembers());
        plan.setActive(true);
        planRepository.save(plan);
    }
}
