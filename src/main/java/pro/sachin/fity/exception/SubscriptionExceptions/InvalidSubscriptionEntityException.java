package pro.sachin.fity.exception.SubscriptionExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class InvalidSubscriptionEntityException extends BusinessExceptionHandler {

    public InvalidSubscriptionEntityException(String message) {
        super(message);
    }
}
