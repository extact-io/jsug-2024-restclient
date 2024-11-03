package sample.spring.book.server.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import sample.spring.book.server.Book;
import sample.spring.book.server.BookRepository;
import sample.spring.book.server.exception.DuplicateException;
import sample.spring.book.server.exception.NotFoundException;

@Repository
public class InMemoryBookRepository implements BookRepository {

    private Map<Integer, Book> bookMap;

    @PostConstruct
    public void init() {
        bookMap = new ConcurrentHashMap<>();
        bookMap.put(1, new Book(1, "燃えよ剣", "司馬遼太郎"));
        bookMap.put(2, new Book(2, "峠", "司馬遼太郎"));
        bookMap.put(3, new Book(3, "ノルウェーの森", "村上春樹"));
    }

    @Override
    public Optional<Book> get(int id) {
        return Optional.ofNullable(bookMap.get(id));
    }

    @Override
    public List<Book> findByAuthorStartingWith(String prefix) {
        return bookMap.values().stream()
                .filter(book -> book.getAuthor().startsWith(prefix))
                .sorted((book1, book2) -> Integer.compare(book1.getId(), book2.getId()))
                .toList();
    }

    @Override
    public Book save(Book book) throws DuplicateException, NotFoundException {

        book = (Book) book.copy();
        if (book.getId() != null) { // for update
            if (!bookMap.containsKey(book.getId())) {
                throw new NotFoundException("id:" + book.getId());
            }
            if (findByTitle(book.getTitle())
                    .filter(book::hasSameTitle)
                    .isPresent()) {
                throw new DuplicateException("title:" + book.getTitle());
            }
        } else { // for add
            if (findByTitle(book.getTitle()).isPresent()) {
                throw new DuplicateException("title:" + book.getTitle());
            }
            int nextId = bookMap.keySet().stream().max(Integer::compareTo).get() + 1;
            book.setId(nextId);
        }

        bookMap.put(book.getId(), book);
        return book;
    }

    @Override
    public void remove(int id) throws NotFoundException {
        if (!bookMap.containsKey(id)) {
            throw new NotFoundException("id:" + id);
        }
        bookMap.remove(id);
    }

    public Optional<Book> findByTitle(String title) {
        return bookMap.values().stream()
                .filter(book -> book.getTitle().equals(title))
                .findFirst();
    }

}
