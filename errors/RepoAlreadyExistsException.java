package errors;

public class RepoAlreadyExistsException extends KiwiException {
    public RepoAlreadyExistsException(String message) {
        super(message);
    }
}
