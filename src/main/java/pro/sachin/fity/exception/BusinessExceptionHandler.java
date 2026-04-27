package pro.sachin.fity.exception;

public abstract class BusinessExceptionHandler extends RuntimeException {

    public BusinessExceptionHandler(String message) {
        super(message);
    }
}
