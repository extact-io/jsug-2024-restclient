package sample.spring.book.server;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sample.spring.book.server.Book.Update;
import sample.spring.book.server.exception.DuplicateException;
import sample.spring.book.server.exception.HandledException;
import sample.spring.book.server.exception.NotFoundException;

@RestController
@HandledException
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository repository;

    @GetMapping("/{id}")
    public Book get(@PathVariable int id) {
        return repository.get(id).orElse(null);
    }

    @GetMapping
    public List<Book> getAll() {
        return repository.findAll();
    }

    @GetMapping("/search")
    public List<Book> findByCondition(@RequestParam Map<String, String> queryParams) {
        return repository.findByCondition(queryParams);
    }

    @GetMapping("/author")
    public List<Book> findByAuthorStartingWith(@NotBlank @Size(max= 10) @RequestParam("prefix") String prefix) {
        return repository.findByAuthorStartingWith(prefix);
    }

    @PostMapping
    public Book add(@RequestBody @Validated Book book) throws DuplicateException {
        return repository.save(book);
    }

    @PutMapping
    public Book update(@RequestBody @Validated(Update.class) Book book) throws NotFoundException {
        var ret = repository.save(book);
        return ret;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) throws NotFoundException {
        repository.remove(id);
    }
}
