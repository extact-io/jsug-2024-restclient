package sample.spring.book.stub.exception;

public class DuplicateServerException extends RuntimeException {

    public DuplicateServerException(String message) {
        super(message);
    }

    public DuplicateServerException(String message, Throwable e) {
        super(message, e);
    }
}
