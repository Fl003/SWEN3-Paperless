package at.technikum.paperless.exception;

public class MissingAuthorException extends RuntimeException {
    public MissingAuthorException() { super(
            "No authenticated user found for this operation."
    ); }
}
