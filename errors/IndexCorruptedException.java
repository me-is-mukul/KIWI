package errors;

public class IndexCorruptedException extends KiwiException {
    public IndexCorruptedException(String details) {
        super("Index file is corrupted or unreadable: " + details);
    }
}
