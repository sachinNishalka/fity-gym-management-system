package pro.sachin.fity.exception.DeviceCommandExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class PayloadSerializationException extends BusinessExceptionHandler {

    public PayloadSerializationException(String message) {
        super(message);
    }
}
