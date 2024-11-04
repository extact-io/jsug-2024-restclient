package sample.spring.book.client.infrastructure.exception;

import sample.spring.book.client.infrastructure.exception.BookResponseErrorHandler.ErrorMessage;

public class NotFoundClientException extends RuntimeException {
    public NotFoundClientException(ErrorMessage message) {
        super(message.message());
    }
}
