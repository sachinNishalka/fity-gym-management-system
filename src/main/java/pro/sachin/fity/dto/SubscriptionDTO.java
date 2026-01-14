package pro.sachin.fity.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SubscriptionDTO {

    private int id;

    private Integer memberId;
    private String memberName;

    private Integer familyId;
    private String familyName;

    private Integer planId;
    private String planName;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate dueDate;
    private LocalDate graceEndDate;

    private String status;

}
