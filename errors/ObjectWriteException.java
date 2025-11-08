package errors;

public class ObjectWriteException extends KiwiException {
    public ObjectWriteException(String objectHash, String details) {
        super("Failed to write object (" + objectHash + "): " + details);
    }
}
