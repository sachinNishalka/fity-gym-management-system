package pro.sachin.fity.exception.FamilyExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class FamilyHasActiveMembersException extends BusinessExceptionHandler {

    public FamilyHasActiveMembersException(String message) {
        super(message);
    }
}
