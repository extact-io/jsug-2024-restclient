package sample.spring.book.client;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class BookService {

    private BookClient bookClient;

    public BookService(BookClient bookClient) {
        this.bookClient = bookClient;
    }

    public BookDto get(int id) {
        return bookClient.get(id);
    }

    public List<BookDto> findByAuthorStartingWith(String prefix) {
        return bookClient.findByAuthorStartingWith(prefix);
    }

    public BookDto add(BookDto book) {
        return bookClient.add(book);
    }

    public BookDto update(BookDto book) {
        return bookClient.update(book);
    }

    public void delete(int id) {
        bookClient.delete(id);
    }
}
