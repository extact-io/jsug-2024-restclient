package sample.spring.book.server;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import sample.spring.book.server.client.UserHeaderRequestInitializer;
import sample.spring.book.server.repository.InMemoryBookRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookControllerTest {

    private BookControllerClient target;
    private RestClient client;

    private final Book expectedBook1 = new Book(1, "燃えよ剣", "司馬遼太郎");
    private final Book expectedBook2 = new Book(2, "峠", "司馬遼太郎");

    @Configuration(proxyBeanMethods = false)
    @Import(BookApplication.class)
    static class TestConfig {

        @Bean
        @Scope("prototype")
        @Primary
        BookRepository bookRepository() {
            return new InMemoryBookRepository();
        }
    }

    @BeforeEach
    void beforeEach(@Value("${local.server.port}") int port) {

        SecurityContextHolder.clearContext();

        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:%s".formatted(port))
                .defaultHeader("Sender-Name", BookApplication.class.getSimpleName())
                .defaultUriVariables(Map.of("name", "ogiwara"))
                .requestInitializer(new UserHeaderRequestInitializer())
                .build();

        this.client = restClient;

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        target = factory.createClient(BookControllerClient.class);
    }

    @Test
    void testGet() {

        Book actual = target.get(1);
        assertThat(actual).isEqualTo(expectedBook1);

        actual = target.get(999);
        assertThat(actual).isNull();
    }

    @Test
    void testGet2() {

        Book actual = target.get(1);
        assertThat(actual).isEqualTo(expectedBook1);

        actual = target.get(999);
        assertThat(actual).isNull();
    }

    @Test
    void testFindByAuthorStartingWith() {

        List<Book> actual = target.findByAuthorStartingWith("司馬");
        assertThat(actual).containsExactly(expectedBook1, expectedBook2);

        actual = target.findByAuthorStartingWith("unknown");
        assertThat(actual).isEmpty();
    }

    @Test
    void testAddToUpdateToDelete() {

        prepareSecurityContext();

        Book addBook = new Book(null, "新宿鮫", "大沢在昌");
        Book actual = target.add(addBook);
        assertThat(actual.getId()).isEqualTo(4);

        Book updateBook = actual.copy();
        updateBook.setTitle("update-title");
        updateBook.setAuthor("update-author");

        actual = target.update(updateBook);
        assertThat(actual.getTitle()).isEqualTo("update-title");
        assertThat(actual.getAuthor()).isEqualTo("update-author");

        target.delete(actual.getId());
        assertThat(target.get(actual.getId())).isNull();
    }

    @Test
    void testAddOccurValidationError() {

        prepareSecurityContext();

        Book addBook = new Book(null, null, null);
        assertThatThrownBy(() -> target.add(addBook))
            .isInstanceOfSatisfying(
                    HttpClientErrorException.class,
                    e -> assertThat(e.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    void testUpdateOccurValidationError() {

        prepareSecurityContext();

        Book addBook = new Book(null, null, null);
        assertThatThrownBy(() -> target.update(addBook))
            .isInstanceOfSatisfying(
                    HttpClientErrorException.class,
                    e -> assertThat(e.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    void testAddOccurDuplicateException() {

        prepareSecurityContext();

        Book addBook = new Book(null, "峠", "司馬遼太郎");
        assertThatThrownBy(() -> target.add(addBook))
                .isInstanceOfSatisfying(
                        HttpClientErrorException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value()));
    }

    @Test
    void testUpdateOccurNotFoundException() {

        prepareSecurityContext();

        Book updateBook = new Book(999, "新宿鮫", "大沢在昌");
        assertThatThrownBy(() -> target.update(updateBook))
                .isInstanceOfSatisfying(
                        HttpClientErrorException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void testDeleteOccurNotFoundException() {

        prepareSecurityContext();

        assertThatThrownBy(() -> target.delete(999))
                .isInstanceOfSatisfying(
                        HttpClientErrorException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void testUpdateOccurConstrainException() {

        prepareSecurityContext();

        assertThatThrownBy(() -> target.update(new Book(3, "燃えよ剣", null)))
                .isInstanceOfSatisfying(
                        HttpClientErrorException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value()));
    }

    private void prepareSecurityContext() {
        Authentication auth = new TestingAuthenticationToken("ID0001", "test", "MEMBER");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @HttpExchange(url = "/books")
    static interface BookControllerClient {

        @GetExchange("/{id}")
        Book get(@PathVariable int id);

        @GetExchange("/author")
        List<Book> findByAuthorStartingWith(@RequestParam("prefix") String prefix);

        @PostExchange
        Book add(@RequestBody Book book);

        @PutExchange
        Book update(@RequestBody Book book);

        @DeleteExchange("/{id}")
        void delete(@PathVariable int id);
    }
}