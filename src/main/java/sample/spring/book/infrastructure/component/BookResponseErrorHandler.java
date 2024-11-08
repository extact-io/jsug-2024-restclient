package sample.spring.book.infrastructure.component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.ResponseErrorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import sample.spring.book.exception.DuplicateException;
import sample.spring.book.exception.ErrorMessage;
import sample.spring.book.exception.NotFoundException;
import sample.spring.book.exception.ValidationException;

@Slf4j
public class BookResponseErrorHandler implements ResponseErrorHandler {

    private static final List<Integer> HANDLE_STATUS = List.of(
            HttpStatus.CONFLICT.value(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.BAD_REQUEST.value());

    @Override
    public boolean hasError(ClientHttpResponse res) throws IOException {
        return HANDLE_STATUS.contains(res.getStatusCode().value());
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

        ErrorMessage message = readBody(response);
        HttpStatus status = HttpStatus.resolve(response.getStatusCode().value());

        switch (status) {
            case HttpStatus.CONFLICT -> throw new DuplicateException(message);
            case HttpStatus.NOT_FOUND -> throw new NotFoundException(message);
            case HttpStatus.BAD_REQUEST -> throw new ValidationException(message);
            default -> throw new IllegalArgumentException("Unexpected value: " + response.getStatusCode());
        }
    }

    private ObjectMapper mapper = new ObjectMapper();

    private ErrorMessage readBody(ClientHttpResponse response) {
        try {
            byte[] body = FileCopyUtils.copyToByteArray(response.getBody());
            String bodyString = new String(body, StandardCharsets.UTF_8);
            return mapper.readValue(bodyString, ErrorMessage.class);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            return new ErrorMessage(e.getMessage());
        }
    }
}
