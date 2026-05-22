package pro.sachin.fity.dto;

import java.math.BigDecimal;

import lombok.Data;
import pro.sachin.fity.model.Subscription;

@Data
public class PartiallyPaidSubscriptionDTO {
    private Long subscriptionId;

    private BigDecimal paid;
    private BigDecimal balance;
    private BigDecimal totalAmount;

    private String memberFirstName;
    private String memberLastName;
    private Long memberId;

    private String planName;
    private String planType;
    private Long planId;

}
