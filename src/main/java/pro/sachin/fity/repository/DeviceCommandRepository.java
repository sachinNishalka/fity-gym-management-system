package pro.sachin.fity.repository;

public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {
    List<DeviceCommand> findByStatusOrderByCreatedAtAsc(CommandStatus status);
}