package pro.sachin.fity.exception.MemberExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class MemberNotFoundException extends BusinessExceptionHandler {

    public MemberNotFoundException(String message) {
        super(message);
    }
}
