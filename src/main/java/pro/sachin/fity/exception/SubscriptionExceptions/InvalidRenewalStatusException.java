package pro.sachin.fity.exception.SubscriptionExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class InvalidRenewalStatusException extends BusinessExceptionHandler {

    public InvalidRenewalStatusException(String message) {
        super(message);
    }
}
