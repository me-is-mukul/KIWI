package errors;


public class KiwiException extends Exception {
    public KiwiException(String message) {
        super(message);
    }

    public KiwiException(String message, Throwable cause) {
        super(message, cause);
    }
}