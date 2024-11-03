package sample.spring.book.client.exception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.ResponseErrorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BookResponseErrorHandler implements ResponseErrorHandler {

    private static final List<Integer> HANDLE_STATUS = List.of(
            HttpStatus.CONFLICT.value(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.BAD_REQUEST.value());

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean hasError(ClientHttpResponse res) throws IOException {
        return HANDLE_STATUS.contains(res.getStatusCode().value());
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

        ErrorMessage message = readBody(response);
        HttpStatus status = HttpStatus.resolve(response.getStatusCode().value());

        switch (status) {
            case HttpStatus.CONFLICT -> throw new DuplicateClientException(message);
            case HttpStatus.NOT_FOUND -> throw new NotFoundClientException(message);
            case HttpStatus.BAD_REQUEST -> throw new ValidationClientException(message);
            default -> throw new IllegalArgumentException("Unexpected value: " + response.getStatusCode());
        }
    }

    private ErrorMessage readBody(ClientHttpResponse response) {

        try {
            byte[] body = FileCopyUtils.copyToByteArray(response.getBody());
            return mapper.readValue(new String(body, StandardCharsets.UTF_8), ErrorMessage.class);

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            ErrorMessage message = new ErrorMessage();
            message.setMessage(e.getMessage());
            return message;

        }
    }

    @Setter @Getter @ToString
    public static class ErrorMessage {
        private String message;
    }
}
