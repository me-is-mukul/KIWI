package errors;

public class CommitException extends KiwiException {
    public CommitException(String details) {
        super("Commit failed: " + details);
    }
}
