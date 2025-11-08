package errors;

public class InvalidCommandException extends KiwiException {
    public InvalidCommandException(String command) {
        super("Unknown or invalid command: " + command);
    }
}
