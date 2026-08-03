package pro.sachin.fity.exception.FamilyExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class MemberNotInFamilyException extends BusinessExceptionHandler {

    public MemberNotInFamilyException(String message) {
        super(message);
    }
}
