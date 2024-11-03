package sample.spring.book.client.exception;

import sample.spring.book.client.exception.BookResponseErrorHandler.ErrorMessage;

public class NotFoundClientException extends RuntimeException {

    private ErrorMessage message;

    public NotFoundClientException(ErrorMessage message) {
        super(message.toString());
        this.message = message;
    }
}
