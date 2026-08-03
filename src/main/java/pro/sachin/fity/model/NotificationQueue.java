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
@Table(name = "notification_queue")
public class NotificationQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    // Who receives the notification
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // What subscription this is about
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    // Type of notification
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    // When to send
    @Column(name = "scheduled_for", nullable = false)
    private LocalDateTime scheduledFor;

    // When it was actually sent (null if not sent yet)
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    // Current status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    // Delivery channel
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel = NotificationChannel.SMS;

    // Optional: Store the message content
    @Transient // Not in database yet
    private String messageContent;
}
