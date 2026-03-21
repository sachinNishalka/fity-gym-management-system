package pro.sachin.fity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


import pro.sachin.fity.model.CommandStatus;
import pro.sachin.fity.model.DeviceCommand;

public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {
    List<DeviceCommand> findByStatusOrderByCreatedAtAsc(CommandStatus status);
}