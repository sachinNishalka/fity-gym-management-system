package pro.sachin.fity.sercives.impl;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import pro.sachin.fity.dto.PlanDTO;
import pro.sachin.fity.mapper.PlanMapper;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.model.Subscription;
import pro.sachin.fity.repository.PlanRepository;
import pro.sachin.fity.repository.SubscriptionRepository;
import pro.sachin.fity.sercives.PlanService;

@RequiredArgsConstructor
@Service
public class PlanServiceImpl implements PlanService {
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanMapper planMapper;

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
    public PlanDTO getPlanById(Long id) {

        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no plan with id " + id));

        int numOfSubscriptions = subscriptionRepository.findByPlan(plan).size();

        PlanDTO planDTO = new PlanDTO();

        planDTO = planMapper.toDto(plan);

        planDTO.setSubscriptionCount(numOfSubscriptions);

        return planDTO;
    }

    @Override
    public PlanDTO updatePlan(Long id, PlanDTO planDTO) {
        Plan existingPlan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no plan with id " + id));
        existingPlan.setName(planDTO.getName());
        existingPlan.setPlanType(planDTO.getPlanType());
        existingPlan.setPrice(planDTO.getPrice());
        existingPlan.setDurationDays(planDTO.getDurationDays());
        existingPlan.setAgeMin(planDTO.getAgeMin());
        existingPlan.setAgeMax(planDTO.getAgeMax());
        existingPlan.setMaximumFamilyMembers(planDTO.getMaximumFamilyMembers());
        Plan updatedPlan = planRepository.save(existingPlan);
        PlanDTO updatedPlanDTO = planMapper.toDto(updatedPlan);
        return updatedPlanDTO;
    }

    @Override
    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }

    @Override
    public List<PlanDTO> getAllPlansWithSubscriptionsCount() {
        List<PlanDTO> planDTOList = new ArrayList<>();

        List<Plan> plansList = planRepository.findAll();

        for (Plan plan : plansList) {
            List<Subscription> subscriptionList = subscriptionRepository.findByPlan(plan);
            PlanDTO planDTO = planMapper.toDto(plan);
            planDTO.setSubscriptionCount(subscriptionList.size());
            planDTOList.add(planDTO);
        }

        return planDTOList;

    }

}
