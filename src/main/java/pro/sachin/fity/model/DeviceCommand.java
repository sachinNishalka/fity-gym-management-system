package pro.sachin.fity.model;

@Entity
@Data
@Table(name = "device_commands")
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "command_type")
    private CommandType commandType;

    @Column(nullable = false, name = "member_id")
    private Long memberId;

    @Column(columnDefinition = "TEXT")
    private String payload;  // JSON string with the data

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommandStatus status;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
