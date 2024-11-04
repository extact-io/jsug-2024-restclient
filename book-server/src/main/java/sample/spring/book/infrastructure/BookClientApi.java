package sample.spring.book.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange("/books")
public interface BookClientApi {

    @GetExchange("/{id}")
    BookResponse get(@PathVariable int id);

    @GetExchange
    List<BookResponse> getAll();

    @GetExchange("/search")
    List<BookResponse> findByCondition(@RequestParam Map<String, String> queryParams);

    @GetExchange("/author")
    List<BookResponse> findByAuthorStartingWith(@RequestParam("prefix") String prefix);

    @PostExchange
    BookResponse add(@RequestBody AddRequest request);

    @PutExchange
    BookResponse update(@RequestBody UpdateRequest request);

    @DeleteExchange("/{id}")
    void delete(@PathVariable int id);

    @PostExchange("/upload")
    String upload(@RequestHeader MultiValueMap<String, String> headers,
            @RequestPart MultiValueMap<String, Resource> parts);

    @GetExchange("/files/{filename:.+}")
    Resource download(@PathVariable String filename);

    @GetExchange("/localdate/{localdate}")
    String pathParamLocalDate(@PathVariable("localdate") LocalDate localdate);

    @GetExchange("/localdate")
    String queryParamLocalDate(@RequestParam("localdate") LocalDate localdate);
}
