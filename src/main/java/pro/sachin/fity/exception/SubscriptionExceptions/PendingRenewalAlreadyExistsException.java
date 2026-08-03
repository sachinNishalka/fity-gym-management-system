package pro.sachin.fity.exception.SubscriptionExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class PendingRenewalAlreadyExistsException extends BusinessExceptionHandler {

    public PendingRenewalAlreadyExistsException(String message) {
        super(message);
    }
}
