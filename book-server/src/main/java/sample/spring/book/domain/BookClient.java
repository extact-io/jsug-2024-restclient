package sample.spring.book.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;

public interface BookClient {

    Optional<Book> get(int id);

    List<Book> getAll();

    List<Book> findByCondition(Map<String, String> queryParams);

    List<Book> findByAuthorStartingWith(String prefix);

    Book add(String title, String author);

    Book update(Book book);

    void delete(int id);

    String upload(String resourceName);

    Resource download(String filename);
}
