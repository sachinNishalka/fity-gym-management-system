package pro.sachin.fity.exception.FamilyExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class MemberAlreadyInFamilyException extends BusinessExceptionHandler {

    public MemberAlreadyInFamilyException(String message) {
        super(message);
    }
}
