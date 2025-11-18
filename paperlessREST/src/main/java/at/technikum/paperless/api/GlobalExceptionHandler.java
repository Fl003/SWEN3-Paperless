package at.technikum.paperless.api;

import at.technikum.paperless.exception.DocumentNotFoundException;
import at.technikum.paperless.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.OffsetDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DocumentNotFoundException.class)
    public ProblemDetail handleDocNotFound(DocumentNotFoundException ex, HttpServletRequest req) {
        log.error("DocumentNotFoundException at {}: {}", req.getRequestURI(), ex.getMessage());
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Document Not Found");
        pd.setType(URI.create("https://errors.paperless/docs/not-found"));
        pd.setProperty("timestamp", OffsetDateTime.now());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(FileStorageException.class)
    public ProblemDetail handleStorage(FileStorageException ex, HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        pd.setTitle("File Storage Error");
        pd.setType(URI.create("https://errors.paperless/docs/storage-error"));
        pd.setProperty("timestamp", OffsetDateTime.now());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    // fallback
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        pd.setTitle("Internal Server Error");
        pd.setProperty("timestamp", OffsetDateTime.now());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }
}
