package pro.sachin.fity.exception.SubscriptionExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class ActiveSubscriptionAlreadyExistsException extends BusinessExceptionHandler {

    public ActiveSubscriptionAlreadyExistsException(String message) {
        super(message);
    }
}
