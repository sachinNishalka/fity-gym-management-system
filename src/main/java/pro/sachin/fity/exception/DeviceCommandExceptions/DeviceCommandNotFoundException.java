package pro.sachin.fity.exception.DeviceCommandExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class DeviceCommandNotFoundException extends BusinessExceptionHandler {

    public DeviceCommandNotFoundException(String message) {
        super(message);
    }
}
