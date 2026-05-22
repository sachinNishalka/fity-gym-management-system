package pro.sachin.fity.dto;

import lombok.Data;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.model.Plan;
import pro.sachin.fity.model.Subscription;

@Data
public class MemberDetailsDTO {
    public Member member;
    public Plan plan;
    public SubscriptionDTO subscription;
    public SubscriptionChargesDTO subscriptionCharges;

}
