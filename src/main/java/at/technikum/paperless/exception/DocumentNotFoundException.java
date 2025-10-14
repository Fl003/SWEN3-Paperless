package at.technikum.paperless.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(long id) {
        super("Document not found: " + id);
    }
}
