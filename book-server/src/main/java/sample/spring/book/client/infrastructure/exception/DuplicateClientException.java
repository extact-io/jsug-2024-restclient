package sample.spring.book.client.infrastructure.exception;

import sample.spring.book.client.infrastructure.exception.BookResponseErrorHandler.ErrorMessage;

public class DuplicateClientException extends RuntimeException {
    public DuplicateClientException(ErrorMessage message) {
        super(message.message());
    }
}
