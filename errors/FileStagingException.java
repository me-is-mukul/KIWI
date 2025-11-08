package errors;

public class FileStagingException extends KiwiException {
    public FileStagingException(String filename, String details) {
        super("Failed to stage file '" + filename + "': " + details);
    }
}
