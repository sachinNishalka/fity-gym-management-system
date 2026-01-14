package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Fetch;

import java.time.LocalDate;

@Entity
@Data
@Check(constraints = "(member_id IS NOT NULL AND family_id IS NULL) OR (member_id IS NULL AND family_id IS NOT NULL)")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int Id;
//    these two can be null, only one member or a family can present in one time
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id")
    private Family family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

//    dates related to the subscription

    @Column(nullable = false, name = "start_date")
    private LocalDate startDate;

//    TODO:this should be calculated - focus on this later
    @Column(nullable = false, name = "end_date")
    private LocalDate endDate;

//    TODO : this also should be calculated - focus on this later
    @Column(nullable = false, name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false, name = "grace_end_date")
    private LocalDate graceEndDate;

    @Column(nullable = false)
    private SubscriptionStatus status;

//    TODO: created by should be included here, but for now the user entity is not available

    @Column(nullable = false, name = "subscribed_date")
    private LocalDate createdAt = LocalDate.now();


//    logic level security implementation, for checking only member or family getting assigned to a subscription not both
    @PrePersist
    @PreUpdate
    private void validateRequestDetailsAtEntityLevel(){
        boolean hasMember = member != null;
        boolean hasFamily = family != null;

        if(hasMember == hasFamily){
//            TODO: this exception should be changed to custom exception handled globally
            throw new IllegalStateException("Subscription must have either member or family, not both");
        }

    }
}
