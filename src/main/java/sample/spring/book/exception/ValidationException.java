package sample.spring.book.exception;

public class ValidationException extends RuntimeException {

    public ValidationException(ErrorMessage message) {
        super(message.message());
    }
}
