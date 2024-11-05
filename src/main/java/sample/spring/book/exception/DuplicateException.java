package sample.spring.book.exception;

public class DuplicateException extends RuntimeException {

    public DuplicateException(ErrorMessage message) {
        super(message.message());
    }
}
