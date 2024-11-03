package sample.spring.book.client.exception;

import sample.spring.book.client.exception.BookResponseErrorHandler.ErrorMessage;

public class ValidationClientException extends RuntimeException {

    private ErrorMessage message;

    public ValidationClientException(ErrorMessage message) {
        super(message.toString());
        this.message = message;
    }
}
