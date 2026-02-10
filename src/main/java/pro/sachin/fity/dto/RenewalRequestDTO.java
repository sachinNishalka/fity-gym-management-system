package pro.sachin.fity.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class RenewalRequestDTO {
    private Long currentSubscriptionId;
    private Long planId;
    private BigDecimal discountAmount;
}