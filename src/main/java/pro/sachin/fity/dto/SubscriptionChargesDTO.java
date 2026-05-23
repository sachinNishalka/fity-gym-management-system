package pro.sachin.fity.dto;

import lombok.Data;

@Data
public class SubscriptionChargesDTO {

    private String status;
    private String startDate;
    private String endDate;
    private String dueDate;
    private String graceEndDate;
    private String totalAmount;
    private String discountAmount;
    private String netAmount;
}