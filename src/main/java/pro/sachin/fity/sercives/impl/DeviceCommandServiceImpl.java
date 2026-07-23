package pro.sachin.fity.sercives.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.sachin.fity.exception.DeviceCommandExceptions.DeviceCommandException;
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

    private static final String BEGIN_TIME = "2024-01-01T00:00:00";
    private static final String BLOCKED_END_TIME = "2020-01-01T00:00:00";

    @Override
    public void queueMemberRegistration(Member member) {

        Map<String, Object> valid = new LinkedHashMap<>();
        valid.put("enable", true);
        valid.put("beginTime", BEGIN_TIME);
        valid.put("endTime", BLOCKED_END_TIME);
        valid.put("timeType", "local");

        Map<String, Object> rightPlanEntry = new LinkedHashMap<>();
        rightPlanEntry.put("doorNo", 1);
        rightPlanEntry.put("planTemplateNo", "1");

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("employeeNo", member.getMemberCode());
        userInfo.put("name", member.getFirstName() + " " + member.getLastName());
        userInfo.put("userType", "normal");
        userInfo.put("Valid", valid);
        userInfo.put("doorRight", "1");
        userInfo.put("RightPlan", List.of(rightPlanEntry));
        userInfo.put("gender", member.getGender() != null ? member.getGender() : "male");
        userInfo.put("localUIRight", false);
        userInfo.put("maxOpenDoorTime", 0);
        userInfo.put("openDoorTime", 0);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("UserInfo", userInfo);

        DeviceCommand command = new DeviceCommand();
        command.setCommandType(CommandType.REGISTER_MEMBER);
        command.setMemberId(member.getId());
        command.setMemberCode(member.getMemberCode());
        command.setPayload(toJson(payload));
        command.setStatus(CommandStatus.PENDING);

        deviceCommandRepository.save(command);
        log.info("Queued REGISTER_MEMBER command for member {}", member.getId());
    }

    @Override
    public void queueAccessUpdate(Long memberId, String memberCode, AccessStatus accessStatus, LocalDate allowedUntil) {
        String endTime;
        if (accessStatus == AccessStatus.ALLOWED && allowedUntil != null) {
            endTime = allowedUntil.atTime(23, 59, 59).toString();
        } else {
            endTime = BLOCKED_END_TIME;
        }

        Map<String, Object> valid = new LinkedHashMap<>();
        valid.put("enable", true);
        valid.put("beginTime", BEGIN_TIME);
        valid.put("endTime", endTime);
        valid.put("timeType", "local");

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("employeeNo", memberCode);
        userInfo.put("Valid", valid);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("UserInfo", userInfo);

        DeviceCommand command = new DeviceCommand();
        command.setCommandType(CommandType.UPDATE_ACCESS);
        command.setMemberId(memberId);
        command.setMemberCode(memberCode);
        command.setPayload(toJson(payload));
        command.setStatus(CommandStatus.PENDING);

        deviceCommandRepository.save(command);
        log.info("Queued UPDATE_ACCESS command for member {} -> endTime: {}", memberId, endTime);
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
            throw new DeviceCommandException("Failed to serialize command payload");
        }
    }
}