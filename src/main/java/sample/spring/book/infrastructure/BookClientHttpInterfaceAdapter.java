package sample.spring.book.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.RequiredArgsConstructor;
import sample.spring.book.domain.Book;
import sample.spring.book.domain.BookClient;
import sample.spring.book.exception.DuplicateException;
import sample.spring.book.exception.NotFoundException;

@RequiredArgsConstructor
public class BookClientHttpInterfaceAdapter implements BookClient {

    private final BookClientApi client;

    @Override
    public Optional<Book> get(int id) {
        BookResponse book = client.get(id);
        return Optional.ofNullable(book)
                .map(BookResponse::toModel);
    }

    @Override
    public List<Book> getAll() {
        return client.getAll().stream()
                .map(BookResponse::toModel)
                .toList();
    }

    @Override
    public List<Book> findByCondition(Map<String, String> queryParams) {
        return client.findByCondition(queryParams).stream()
                .map(BookResponse::toModel)
                .toList();
    }

    @Override
    public List<Book> findByAuthorStartingWith(String prefix) {
        return client.findByAuthorStartingWith(prefix).stream()
                .map(BookResponse::toModel)
                .toList();
    }

    @Override
    public Book add(String title, String author, LocalDate published) throws DuplicateException {
        AddRequest request = new AddRequest(title, author, published);
        return client.add(request).toModel();
    }

    @Override
    public Book update(Book updateBook) throws NotFoundException {
        UpdateRequest request = UpdateRequest.from(updateBook);
        return client.update(request).toModel();
    }

    @Override
    public void delete(int id) throws NotFoundException {
        client.delete(id);
    }

    @Override
    public String upload(String resourceName) {

        MultiValueMap<String, Resource> parts = new LinkedMultiValueMap<>();
        parts.add("file", new ClassPathResource(resourceName));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return client.upload(headers, parts);
    }

    @Override
    public Resource download(String fileName) {
        return client.download(fileName);
    }

    @Override
    public String pathParamLocalDate(LocalDate localDate) {
        return client.pathParamLocalDate(localDate);
    }

    @Override
    public String queryParamLocalDate(LocalDate localDate) {
        return client.queryParamLocalDate(localDate);
    }

    @Override
    public Book badReturnModel() {
        return client.badResponse().toModel();
    }
}
