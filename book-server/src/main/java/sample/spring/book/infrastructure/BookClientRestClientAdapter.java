package sample.spring.book.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import sample.spring.book.domain.Book;
import sample.spring.book.domain.BookClient;
import sample.spring.book.exception.DuplicateException;
import sample.spring.book.exception.NotFoundException;

@RequiredArgsConstructor
public class BookClientRestClientAdapter implements BookClient {

    private final RestClient client;

    @Override
    public Optional<Book> get(int id) {

        BookResponse book = client
                .get()
                .uri("/books/{id}", id)
                .retrieve()
                .body(BookResponse.class);

        return Optional.ofNullable(book)
                .map(BookResponse::toModel);
    }

    @Override
    public List<Book> getAll() {

        List<BookResponse> bookResponses= client
                .get()
                .uri("/books")
                .retrieve()
                .body(new ParameterizedTypeReference<List<BookResponse>>() {});

        return bookResponses.stream()
                .map(BookResponse::toModel)
                .toList();
    }

    @Override
    public List<Book> findByCondition(Map<String, String> queryParams) {

        // 型をUriBuilderが要求するMultiValueMapに変換
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        queryParams.forEach((key, value) -> multiValueMap.add(key, value));

        List<BookResponse> bookResponses= client
                .get()
                .uri("/books/search", builder -> builder
                        .queryParams(multiValueMap)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<BookResponse>>() {});

        return bookResponses.stream()
                .map(BookResponse::toModel)
                .toList();
    }

    @Override
    public List<Book> findByAuthorStartingWith(String prefix) {

        List<BookResponse> bookResponses= client
                .get()
                .uri("/books/author", builder -> builder.queryParam("prefix", prefix).build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        return bookResponses.stream()
                .map(BookResponse::toModel)
                .toList();
    }

    @Override
    public Book add(String title, String author, LocalDate published) throws DuplicateException {

        AddRequest request = new AddRequest(title, author, published);

        BookResponse bookResponse = client
                .post()
                .uri("/books")
                .body(request)
                .retrieve()
                .body(BookResponse.class);
        return bookResponse.toModel();
    }

    public Book update(Book updateBook) throws NotFoundException {

        UpdateRequest request = UpdateRequest.from(updateBook);

        BookResponse bookResponse = client
                .put()
                .uri("/books")
                .body(request)
                .retrieve()
                .body(BookResponse.class);
        return bookResponse.toModel();
    }

    @Override
    public void delete(int id) throws NotFoundException {
        client.delete()
                .uri("/books/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public String upload(String resourceName) {

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new ClassPathResource(resourceName));

        return client
                .post()
                .uri("/books/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .body(String.class);
    }

    @Override
    public Resource download(String fileName) {
        return client
                .get()
                .uri("/books/files/{filename}", fileName)
                .retrieve()
                .body(Resource.class);
    }
}
