package pro.sachin.fity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.sercives.PlanService;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/plan")
public class PlanController {

    private final PlanService planService;

//    TODO: plan creation endpoint
    @PostMapping("/create")
    ResponseEntity<Plan> createPlan(@RequestBody Plan plan){
        planService.createPlan(plan);
        return ResponseEntity.ok(plan);
    }
}
