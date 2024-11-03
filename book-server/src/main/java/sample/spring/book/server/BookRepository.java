package sample.spring.book.server;

import java.util.List;
import java.util.Optional;

import sample.spring.book.server.exception.DuplicateException;
import sample.spring.book.server.exception.NotFoundException;

public interface BookRepository {

    Optional<Book> get(int id);

    List<Book> findByAuthorStartingWith(String prefix);

    Book save(Book entty) throws DuplicateException, NotFoundException;

    void remove(int id) throws NotFoundException;
}
