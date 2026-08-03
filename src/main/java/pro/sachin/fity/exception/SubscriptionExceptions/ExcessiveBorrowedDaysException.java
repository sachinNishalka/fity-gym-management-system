package pro.sachin.fity.exception.SubscriptionExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class ExcessiveBorrowedDaysException extends BusinessExceptionHandler {

    public ExcessiveBorrowedDaysException(String message) {
        super(message);
    }
}
