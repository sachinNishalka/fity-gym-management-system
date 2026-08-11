package pro.sachin.fity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class RenewalRequestDTO {
    private Long currentSubscriptionId;
    private Long planId;
    private BigDecimal discountAmount;
    private LocalDate startDate;
}