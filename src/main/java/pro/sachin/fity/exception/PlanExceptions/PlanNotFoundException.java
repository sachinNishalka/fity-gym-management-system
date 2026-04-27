package pro.sachin.fity.exception.PlanExceptions;

import pro.sachin.fity.exception.BusinessExceptionHandler;

public class PlanNotFoundException extends BusinessExceptionHandler {

    public PlanNotFoundException(String message) {
        super(message);
    }
}
