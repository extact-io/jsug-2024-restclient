package sample.spring.book.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

@Validated
public interface BookClient {

    Optional<Book> get(int id);

    List<Book> getAll();

    List<Book> findByCondition(Map<String, String> queryParams);

    List<Book> findByAuthorStartingWith(String prefix);

    Book add(String title, String author, LocalDate published);

    Book update(Book book);

    void delete(int id);

    String upload(String resourceName);

    Resource download(String filename);

    String pathParamLocalDate(LocalDate localDate);

    String queryParamLocalDate(LocalDate localDate);

    @Valid
    Book badReturnModel();
}
