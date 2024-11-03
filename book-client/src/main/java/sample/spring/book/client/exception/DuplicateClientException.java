package sample.spring.book.client.exception;

import sample.spring.book.client.exception.BookResponseErrorHandler.ErrorMessage;

public class DuplicateClientException extends RuntimeException {

    private ErrorMessage message;

    public DuplicateClientException(ErrorMessage message) {
        super(message.toString());
        this.message = message;
    }
}
