package pro.sachin.fity.dto;

import java.math.BigDecimal;

import lombok.Data;
import pro.sachin.fity.model.PlanType;

@Data
public class PlanDTO {
    private Long id;
    private String name;
    private Integer durationDays;
    private BigDecimal price;
    private PlanType planType;
    private int ageMin;
    private int ageMax;
    private int maximumFamilyMembers;
    private int subscriptionCount;
}
