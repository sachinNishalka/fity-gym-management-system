package pro.sachin.fity.exception.FamilyExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class FamilyNotFoundException extends BusinessExceptionHandler {

    public FamilyNotFoundException(String message) {
        super(message);
    }
}
