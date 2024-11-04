package sample.spring.book.stub.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import sample.spring.book.stub.BookRepository;
import sample.spring.book.stub.BookServerModel;
import sample.spring.book.stub.exception.DuplicateException;
import sample.spring.book.stub.exception.NotFoundException;

@Repository
public class InMemoryBookRepository implements BookRepository {

    private Map<Integer, BookServerModel> bookMap;

    @PostConstruct
    public void init() {
        bookMap = new ConcurrentHashMap<>();
        bookMap.put(1, new BookServerModel(1, "燃えよ剣", "司馬遼太郎"));
        bookMap.put(2, new BookServerModel(2, "峠", "司馬遼太郎"));
        bookMap.put(3, new BookServerModel(3, "ノルウェーの森", "村上春樹"));
    }

    @Override
    public Optional<BookServerModel> get(int id) {
        return Optional.ofNullable(bookMap.get(id));
    }

    @Override
    public List<BookServerModel> findAll() {
        return bookMap.values().stream()
                .sorted((book1, book2) -> Integer.compare(book1.getId(), book2.getId()))
                .toList();
    }

    // @formatter:off
    @Override
    public List<BookServerModel> findByCondition(Map<String, String> condition) {
        return bookMap.values().stream()
                .filter(book ->
                    condition.entrySet().stream().allMatch(entry ->
                        switch (entry.getKey()) {
                            case "id" -> Objects.equals(entry.getValue(), String.valueOf(book.getId()));
                            case "title" -> Objects.equals(entry.getValue(), book.getTitle());
                            case "author" -> Objects.equals(entry.getValue(), book.getAuthor());
                            default -> true;
                }))
                .toList();
    }
    // @formatter:on

    @Override
    public List<BookServerModel> findByAuthorStartingWith(String prefix) {
        return bookMap.values().stream()
                .filter(book -> book.getAuthor().startsWith(prefix))
                .sorted((book1, book2) -> Integer.compare(book1.getId(), book2.getId()))
                .toList();
    }

    @Override
    public BookServerModel save(BookServerModel book) throws DuplicateException, NotFoundException {

        book = book.copy();
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

    private Optional<BookServerModel> findByTitle(String title) {
        return bookMap.values().stream()
                .filter(book -> book.getTitle().equals(title))
                .findFirst();
    }
}
