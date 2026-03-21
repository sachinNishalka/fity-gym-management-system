package pro.sachin.fity.sercives;

import java.time.LocalDate;
import java.util.List;

import pro.sachin.fity.model.AccessStatus;
import pro.sachin.fity.model.DeviceCommand;
import pro.sachin.fity.model.Member;

public interface DeviceCommandService {

    void queueMemberRegistration(Member member);

    void queueAccessUpdate(Long memberId, AccessStatus accessStatus, LocalDate allowedUntil);

    List<DeviceCommand> getPendingCommands();

    void acknowledgeCommand(Long commandId, boolean success);
}