package pro.sachin.fity.sercives;

public interface DeviceCommandService {

    void queueMemberRegistration(Member member);

    void queueAccessUpdate(Long memberId, AccessStatus accessStatus);

    List<DeviceCommand> getPendingCommands();

    void acknowledgeCommand(Long commandId, boolean success);
}