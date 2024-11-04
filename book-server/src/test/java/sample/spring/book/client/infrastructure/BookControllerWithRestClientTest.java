package sample.spring.book.client.infrastructure;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import sample.spring.book.client.domain.Book;
import sample.spring.book.client.domain.BookClient;
import sample.spring.book.client.infrastructure.exception.BookResponseErrorHandler;
import sample.spring.book.client.infrastructure.exception.DuplicateClientException;
import sample.spring.book.client.infrastructure.exception.NotFoundClientException;
import sample.spring.book.client.infrastructure.exception.ValidationClientException;
import sample.spring.book.stub.BookApplication;
import sample.spring.book.stub.BookRepository;
import sample.spring.book.stub.impl.InMemoryBookRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookControllerWithRestClientTest {

    private BookClient client;

    private final Book expectedBook1 = new Book(1, "燃えよ剣", "司馬遼太郎");
    private final Book expectedBook2 = new Book(2, "峠", "司馬遼太郎");
    private final Book expectedBook3 = new Book(3, "ノルウェーの森", "村上春樹");

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

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("http://localhost:" + port)
                .queryParam("Sender-Name", BookApplication.class.getSimpleName());
        UriBuilderFactory factory = new DefaultUriBuilderFactory(builder);

        RestClient restClient = RestClient.builder()
                //.baseUrl("http://localhost:" + port)
                .uriBuilderFactory(factory)
                .defaultHeader("Sender-Name", BookApplication.class.getSimpleName())
                .defaultUriVariables(Map.of("context", "books"))
                .defaultStatusHandler(new BookResponseErrorHandler())
                .requestInitializer(new PropagateUserContextInitializer())
                .build();

        this.client = new BookClientRestClientAdapter(restClient);
    }

    @Test
    void testGet() {

        Optional<Book> actual = client.get(1);
        assertThat(actual).isPresent().contains(expectedBook1);

        actual = client.get(999);
        assertThat(actual).isEmpty();
    }

    @Test
    void testGetAll() {

        List<Book> actual = client.getAll();
        assertThat(actual).containsExactly(expectedBook1, expectedBook2, expectedBook3);
    }

    @Test
    void testFindByCondition() {

        // 全項目一致検索
        Map<String, String> conditions = Map.of(
                "id", "2",
                "title", "峠",
                "author", "司馬遼太郎");

        List<Book> actual = client.findByCondition(conditions);
        assertThat(actual).containsExactly(expectedBook2);


        // 著者名検索
        conditions = Map.of("author", "司馬遼太郎");
        actual = client.findByCondition(conditions);
        assertThat(actual).containsExactly(expectedBook1, expectedBook2);


        // idと著者名で検索
        conditions = Map.of(
                "id", "1",
                "author", "司馬遼太郎");
        actual = client.findByCondition(conditions);
        assertThat(actual).containsExactly(expectedBook1);


        // 該当なし（一部項目不一致）
        conditions = Map.of(
                "id", "9",
                "author", "司馬遼太郎");
        actual = client.findByCondition(conditions);
        assertThat(actual).isEmpty();
    }

    @Test
    void testFindByAuthorStartingWith() {

        List<Book> actual = client.findByAuthorStartingWith("司馬");
        assertThat(actual).containsExactly(expectedBook1, expectedBook2);

        actual = client.findByAuthorStartingWith("unknown");
        assertThat(actual).isEmpty();
    }

    @Test
    void testAdd() {

        prepareSecurityContext();

        Book actual = client.add("新宿鮫", "大沢在昌");

        assertThat(actual.getId()).isEqualTo(4);
    }

    @Test
    void testUpdate() {

        prepareSecurityContext();

        Book updateBook = expectedBook1.copy();
        updateBook.setTitle("update-title");
        updateBook.setAuthor("update-author");

        Book actual = client.update(updateBook);

        assertThat(actual.getId()).isEqualTo(1);
        assertThat(actual.getTitle()).isEqualTo("update-title");
        assertThat(actual.getAuthor()).isEqualTo("update-author");
    }

    @Test
    void testDelete() {

        prepareSecurityContext();

        client.delete(1);
    }

    @Test
    void testUpload() {

        prepareSecurityContext();

        String actual = client.upload("mz-tech-logo-small.png");

        assertThat(actual).isEqualTo("mz-tech-logo-small.png");
    }

    @Test
    void testDownload() {

        prepareSecurityContext();

        Resource actual = client.download("mz-tech-logo-small.png");

        assertThat(actual).isNotNull();
    }


    // ----------------------------------------------------- Exception Testing

    @Test
    void testAddOccurValidationError() {

        prepareSecurityContext();

        assertThatThrownBy(() -> client.add(null, null))
                .isInstanceOf(ValidationClientException.class);
    }

    @Test
    void testUpdateOccurValidationError() {

        prepareSecurityContext();

        assertThatThrownBy(() -> client.update(new Book(999, null, null)))
                .isInstanceOf(ValidationClientException.class);
    }

    @Test
    void testAddOccurDuplicateException() {

        prepareSecurityContext();

        assertThatThrownBy(() -> client.add("峠", "司馬遼太郎"))
                .isInstanceOf(DuplicateClientException.class);
    }

    @Test
    void testUpdateOccurNotFoundException() {

        prepareSecurityContext();

        Book updateBook = new Book(999, "新宿鮫", "大沢在昌");
        assertThatThrownBy(() -> client.update(updateBook))
                .isInstanceOf(NotFoundClientException.class);
    }

    @Test
    void testDeleteOccurNotFoundException() {

        prepareSecurityContext();

        assertThatThrownBy(() -> client.delete(999))
                .isInstanceOf(NotFoundClientException.class);
    }

    @Test
    void testUpdateOccurConstrainException() {

        prepareSecurityContext();

        assertThatThrownBy(() -> client.update(new Book(3, "燃えよ剣", null)))
                .isInstanceOf(DuplicateClientException.class);
    }


    // ----------------------------------------------------- private methods

    private void prepareSecurityContext() {
        Authentication auth = new TestingAuthenticationToken("ID0001", "test", "MEMBER");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


}