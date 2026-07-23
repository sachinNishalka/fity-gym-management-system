package pro.sachin.fity.exception.SubscriptionExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class PendingSubscriptionAlreadyExistsException extends BusinessExceptionHandler {

    public PendingSubscriptionAlreadyExistsException(String message) {
        super(message);
    }
}
