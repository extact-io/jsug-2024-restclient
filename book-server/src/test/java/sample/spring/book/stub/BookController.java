package sample.spring.book.stub;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

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
import sample.spring.book.stub.BookServerModel.Update;
import sample.spring.book.stub.exception.DuplicateServerException;
import sample.spring.book.stub.exception.ExceptionHandled;
import sample.spring.book.stub.exception.NotFoundServerException;

@RestController
@ExceptionHandled
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final ObjectProvider<BookRepository> provider;

    @GetMapping("/{id}")
    public BookServerModel get(@PathVariable int id) {
        return repository().get(id).orElse(null);
    }

    @GetMapping
    public List<BookServerModel> getAll() {
        return repository().findAll();
    }

    @GetMapping("/search")
    public List<BookServerModel> findByCondition(@RequestParam Map<String, String> queryParams) {
        return repository().findByCondition(queryParams);
    }

    @GetMapping("/author")
    public List<BookServerModel> findByAuthorStartingWith(
            @NotBlank @Size(max = 10) @RequestParam("prefix") String prefix) {
        return repository().findByAuthorStartingWith(prefix);
    }

    @PostMapping
    public BookServerModel add(@RequestBody @Validated BookServerModel book) throws DuplicateServerException {
        return repository().save(book);
    }

    @PutMapping
    public BookServerModel update(@RequestBody @Validated({ Update.class, Default.class }) BookServerModel book)
            throws NotFoundServerException {
        return repository().save(book);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) throws NotFoundServerException {
        repository().remove(id);
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

    private BookRepository repository() {
        return provider.getObject();
    }
}
