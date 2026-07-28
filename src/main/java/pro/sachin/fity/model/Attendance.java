package pro.sachin.fity.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;

    // Required: Who checked in
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Auto-captured: When they checked in
    @Column(name = "check_in_time", nullable = false)
    @CreationTimestamp
    private LocalDateTime checkInTime;

    // How was it captured: 'door' or 'manual'
    @Enumerated(EnumType.STRING)
    @Column(name = "captured_by", nullable = false)
    private CapturedBy capturedBy = CapturedBy.DOOR;

    // Optional: Link to which subscription was active
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
}
