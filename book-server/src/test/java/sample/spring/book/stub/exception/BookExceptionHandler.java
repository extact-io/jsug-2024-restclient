package sample.spring.book.stub.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(annotations = ExceptionHandled.class)
public class BookExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundServerException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundServerException e, WebRequest req) {
        return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateServerException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateKeyException(DuplicateServerException e, WebRequest req) {
        return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
    }
}
