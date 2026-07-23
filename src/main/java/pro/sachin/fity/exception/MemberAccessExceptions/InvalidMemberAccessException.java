package pro.sachin.fity.exception.MemberAccessExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class InvalidMemberAccessException extends BusinessExceptionHandler {

    public InvalidMemberAccessException(String message) {
        super(message);
    }
}
