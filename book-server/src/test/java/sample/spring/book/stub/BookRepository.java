package sample.spring.book.stub;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import sample.spring.book.client.domain.Book;
import sample.spring.book.stub.exception.DuplicateException;
import sample.spring.book.stub.exception.NotFoundException;

public interface BookRepository {

    Optional<Book> get(int id);

    List<Book> findAll();

    List<Book> findByCondition(Map<String, String> condition);

    List<Book> findByAuthorStartingWith(String prefix);

    Book save(Book entty) throws DuplicateException, NotFoundException;

    void remove(int id) throws NotFoundException;

}
