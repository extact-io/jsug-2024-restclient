package sample.spring.book.stub;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import sample.spring.book.stub.exception.DuplicateServerException;
import sample.spring.book.stub.exception.NotFoundServerException;

public interface BookRepository {

    Optional<BookServerModel> get(int id);

    List<BookServerModel> findAll();

    List<BookServerModel> findByCondition(Map<String, String> condition);

    List<BookServerModel> findByAuthorStartingWith(String prefix);

    BookServerModel save(BookServerModel entty) throws DuplicateServerException, NotFoundServerException;

    void remove(int id) throws NotFoundServerException;

}
