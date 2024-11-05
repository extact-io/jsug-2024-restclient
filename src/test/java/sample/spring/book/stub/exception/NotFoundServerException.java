package sample.spring.book.stub.exception;

public class NotFoundServerException extends RuntimeException {

    public NotFoundServerException(String message) {
        super(message);
    }
}
