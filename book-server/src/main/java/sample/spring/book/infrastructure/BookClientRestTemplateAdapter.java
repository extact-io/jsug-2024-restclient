package sample.spring.book.infrastructure;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import lombok.RequiredArgsConstructor;
import sample.spring.book.domain.Book;
import sample.spring.book.domain.BookClient;
import sample.spring.book.exception.DuplicateException;
import sample.spring.book.exception.NotFoundException;

// やりたいことは同じでもgetForObject, getForEntity, exchangeと3つもある
// それぞれ引数を沢山とるのでどこになにを渡せばいいのかわからない。（いつも調べるのめんどくさい）
// 加えてオーバーロードメソッドが沢山あるので、どれを使えばいいのかさらにわかりずらい
// 加えてラムダも使えないので記述が冗長になりがち（最近のスタイルと比べて）
@RequiredArgsConstructor
public class BookClientRestTemplateAdapter implements BookClient {

    private final RestTemplate restTemplate;

    @Override
    public Optional<Book> get(int id) {

        BookResponse book = restTemplate.getForObject("/books/{id}", BookResponse.class, id);

        return Optional.ofNullable(book)
                .map(BookResponse::toModel);
    }

    @Override
    public List<Book> getAll() {

        // getForObjectとgetForEntityではBodyの取得型にListを使えない
        List<BookResponse> bookResponses = restTemplate.exchange(
                "/books",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BookResponse>>() {
                })
                .getBody();

        return bookResponses.stream()
                .map(BookResponse::toModel)
                .toList();
    }

    @Override
    public List<Book> findByCondition(Map<String, String> queryParams) {

        UriBuilder builder;
        if (restTemplate.getUriTemplateHandler() instanceof UriBuilderFactory factory) {
            builder = factory.builder();
        } else {
            throw new IllegalStateException("unknwon type =>" + restTemplate.getUriTemplateHandler().getClass());
        }

        builder.path("/books/search");

        // Mapの内容をUriComponentsBuilderを使ってクエリーストリングに変換
        queryParams.forEach((key, value) -> builder.queryParam(key, value));
        URI uri = builder.build();

        List<BookResponse> bookResponses = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BookResponse>>() {
                })
                .getBody();

        return bookResponses.stream()
                .map(BookResponse::toModel)
                .toList();
    }

    @Override
    public List<Book> findByAuthorStartingWith(String prefix) {

        List<BookResponse> bookResponses = restTemplate.exchange(
                "/books/author?prefix={prefix}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BookResponse>>() {
                },
                prefix)
                .getBody();

        return bookResponses.stream()
                .map(BookResponse::toModel)
                .toList();
    }

    @Override
    public Book add(String title, String author) throws DuplicateException {
        AddRequest request = new AddRequest(title, author);
        BookResponse bookResponse = restTemplate.postForObject("/books", request, BookResponse.class);
        return bookResponse.toModel();
    }

    public Book update(Book updateBook) throws NotFoundException {

        UpdateRequest request = UpdateRequest.from(updateBook);

        // PUTは戻り値がないのでexchangeを使用する
        BookResponse bookResponse = restTemplate.exchange(
                "/books",
                HttpMethod.PUT,
                new HttpEntity<>(request),
                BookResponse.class)
                .getBody();

        return bookResponse.toModel();
    }

    @Override
    public void delete(int id) throws NotFoundException {
        restTemplate.delete("/books/{id}", id);
    }

    @Override
    public String upload(String resourceName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource download(String filename) {
        throw new UnsupportedOperationException();
    }
}
