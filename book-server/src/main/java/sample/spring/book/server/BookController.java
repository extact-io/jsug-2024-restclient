package sample.spring.book.server;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;

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

    private final ObjectProvider<BookRepository> provider;

    @GetMapping("/{id}")
    public Book get(@PathVariable int id) {
        return provider.getObject().get(id).orElse(null);
    }

    @GetMapping
    public List<Book> getAll() {
        return provider.getObject().findAll();
    }

    @GetMapping("/search")
    public List<Book> findByCondition(@RequestParam Map<String, String> queryParams) {
        return provider.getObject().findByCondition(queryParams);
    }

    @GetMapping("/author")
    public List<Book> findByAuthorStartingWith(@NotBlank @Size(max= 10) @RequestParam("prefix") String prefix) {
        return provider.getObject().findByAuthorStartingWith(prefix);
    }

    @PostMapping
    public Book add(@RequestBody @Validated Book book) throws DuplicateException {
        return provider.getObject().save(book);
    }

    @PutMapping
    public Book update(@RequestBody @Validated(Update.class) Book book) throws NotFoundException {
        return provider.getObject().save(book);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) throws NotFoundException {
        provider.getObject().remove(id);
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        return file.getOriginalFilename();
    }

    @GetMapping("/files/{filename:.+}")
    ResponseEntity<Resource> download(@PathVariable String filename) {

        Resource resource = new ClassPathResource(filename);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        ContentDisposition cd = ContentDisposition
                .attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(cd);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
