package sample.spring.book.client;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange(url = "/books")
public interface BookClient {

    @GetExchange("/{id}")
    BookDto get(@PathVariable int id);

    @GetExchange("/author")
    List<BookDto> findByAuthorStartingWith(@RequestParam("prefix") String prefix);

    @PostExchange
    BookDto add(@RequestBody BookDto book);

    @PutExchange
    BookDto update(@RequestBody BookDto book);

    @DeleteExchange("/{id}")
    void delete(@PathVariable int id);
}
