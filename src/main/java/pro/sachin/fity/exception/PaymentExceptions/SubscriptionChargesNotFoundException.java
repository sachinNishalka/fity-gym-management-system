package pro.sachin.fity.exception.PaymentExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class SubscriptionChargesNotFoundException extends BusinessExceptionHandler {

    public SubscriptionChargesNotFoundException(String message) {
        super(message);
    }
}
