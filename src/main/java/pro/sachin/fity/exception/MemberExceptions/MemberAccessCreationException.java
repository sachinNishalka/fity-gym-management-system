package pro.sachin.fity.exception.MemberExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class MemberAccessCreationException extends BusinessExceptionHandler {

    public MemberAccessCreationException(String message) {
        super(message);
    }
}
