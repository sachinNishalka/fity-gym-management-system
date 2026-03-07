package pro.sachin.fity.sercives.impl;


import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
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
    @Override
    public List<Plan> getAllPlans() {
       return planRepository.findAll();
    }
    @Override
    public Plan getPlanById(Long id) {
        return planRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("There is no plan with id "+ id));          
    }
    @Override
    public Plan updatePlan(Long id, PlanDTO planDTO) {
        Plan existingPlan = planRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("There is no plan with id "+ id));          
        existingPlan.setName(planDTO.getName());
        existingPlan.setPlanType(planDTO.getPlanType());
        existingPlan.setPrice(planDTO.getPrice());
        existingPlan.setDurationDays(planDTO.getDurationDays());
        existingPlan.setAgeMin(planDTO.getAgeMin());
        existingPlan.setAgeMax(planDTO.getAgeMax());
        existingPlan.setMaximumFamilyMembers(planDTO.getMaximumFamilyMembers());
        return planRepository.save(existingPlan);
    }
    @Override
    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }

    
}
