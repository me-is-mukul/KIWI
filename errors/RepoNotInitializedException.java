package errors;

public class RepoNotInitializedException extends KiwiException {
    public RepoNotInitializedException() {
        super("Repository not initialized. Run 'kiwi init' first.");
    }
}
