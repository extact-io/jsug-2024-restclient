package sample.spring.book.client.infrastructure.exception;

import sample.spring.book.client.infrastructure.exception.BookResponseErrorHandler.ErrorMessage;

public class ValidationClientException extends RuntimeException {
    public ValidationClientException(ErrorMessage message) {
        super(message.message());
    }
}
