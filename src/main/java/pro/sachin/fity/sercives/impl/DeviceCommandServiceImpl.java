package pro.sachin.fity.sercives.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.CommandStatus;
import pro.sachin.fity.model.CommandType;
import pro.sachin.fity.model.DeviceCommand;
import pro.sachin.fity.model.Member;
import pro.sachin.fity.repository.DeviceCommandRepository;
import pro.sachin.fity.sercives.DeviceCommandService;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceCommandServiceImpl implements DeviceCommandService {

    private final DeviceCommandRepository deviceCommandRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void queueMemberRegistration(Member member) {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("memberId", member.getId());
        payloadMap.put("firstName", member.getFirstName());
        payloadMap.put("lastName", member.getLastName());
        payloadMap.put("access", false);

        DeviceCommand command = new DeviceCommand();
        command.setCommandType(CommandType.REGISTER_MEMBER);
        command.setMemberId(member.getId());
        command.setPayload(toJson(payloadMap));
        command.setStatus(CommandStatus.PENDING);

        deviceCommandRepository.save(command);
        log.info("Queued REGISTER_MEMBER command for member {}", member.getId());
    }

    @Override
    public void queueAccessUpdate(Long memberId, AccessStatus accessStatus) {
        boolean access = accessStatus == AccessStatus.ALLOWED;

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("memberId", memberId);
        payloadMap.put("access", access);

        DeviceCommand command = new DeviceCommand();
        command.setCommandType(CommandType.UPDATE_ACCESS);
        command.setMemberId(memberId);
        command.setPayload(toJson(payloadMap));
        command.setStatus(CommandStatus.PENDING);

        deviceCommandRepository.save(command);
        log.info("Queued UPDATE_ACCESS command for member {} -> access: {}", memberId, access);
    }

    @Override
    public List<DeviceCommand> getPendingCommands() {
        return deviceCommandRepository.findByStatusOrderByCreatedAtAsc(CommandStatus.PENDING);
    }

    @Override
    public void acknowledgeCommand(Long commandId, boolean success) {
        DeviceCommand command = deviceCommandRepository.findById(commandId)
                .orElseThrow(() -> new EntityNotFoundException("Command not found with id " + commandId));

        command.setStatus(success ? CommandStatus.COMPLETED : CommandStatus.FAILED);
        command.setProcessedAt(LocalDateTime.now());
        deviceCommandRepository.save(command);

        log.info("Command {} acknowledged with status {}", commandId, command.getStatus());
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload to JSON", e);
            throw new RuntimeException("Failed to serialize command payload", e);
        }
    }
}