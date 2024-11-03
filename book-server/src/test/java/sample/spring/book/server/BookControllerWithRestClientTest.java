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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import sample.spring.book.server.client.UserHeaderRequestInitializer;
import sample.spring.book.server.repository.InMemoryBookRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookControllerWithRestClientTest {

    private RestClient client;

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

        this.client = RestClient.builder()
                //.baseUrl("http://localhost:" + port)
                .uriBuilderFactory(factory)
                .defaultHeader("Sender-Name", BookApplication.class.getSimpleName())
                .defaultUriVariables(Map.of("context", "books"))
                .requestInitializer(new UserHeaderRequestInitializer())
                .build();
    }

    @Test
    void testGet() {

        Book actual = client
                .get()
                .uri("/books/{id}", 1)
                .retrieve()
                .body(Book.class);

        assertThat(actual).isEqualTo(expectedBook1);

        actual = client
                .get()
                .uri("/books/{id}", 999)
                .retrieve()
                .body(Book.class);

        assertThat(actual).isNull();
    }

    @Test
    void testGetAll() {

        List<Book> actual = client
                .get()
                .uri("/books")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Book>>(){});

        assertThat(actual).containsExactly(expectedBook1, expectedBook2, expectedBook3);
    }

    @Test
    void testFindByCondition() {

        // 全項目一致検索
        List<Book> actual = client
                .get()
                .uri("/books/search", builder -> builder
                        .queryParam("id", "2")
                        .queryParam("title", "峠")
                        .queryParam("author", "司馬遼太郎")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Book>>(){});

        assertThat(actual).containsExactly(expectedBook2);

        // 著者名検索
        actual = client
                .get()
                .uri("/books/search", builder -> builder
                        .queryParam("author", "司馬遼太郎")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Book>>(){});

        assertThat(actual).containsExactly(expectedBook1, expectedBook2);

        // idと著者名で検索
        actual = client
                .get()
                .uri("/books/search", builder -> builder
                        .queryParam("id", "1")
                        .queryParam("author", "司馬遼太郎")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Book>>(){});

        assertThat(actual).containsExactly(expectedBook1);

        // 該当なし（一部項目不一致）
        actual = client
                .get()
                .uri("/books/search", builder -> builder
                        .queryParam("id", "9")
                        .queryParam("author", "司馬遼太郎")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Book>>(){});

        assertThat(actual).isEmpty();
    }

    @Test
    void testFindByAuthorStartingWith() {

        List<Book> actual = client
                .get()
                .uri("/books/author", builder -> builder.queryParam("prefix", "司馬").build())
                .retrieve()
                .body(new ParameterizedTypeReference<>(){});

        assertThat(actual).containsExactly(expectedBook1, expectedBook2);

        actual = client
                .get()
                .uri("/books/author", builder -> builder.queryParam("prefix", "unknown").build())
                .retrieve()
                .body(new ParameterizedTypeReference<>(){});

        assertThat(actual).isEmpty();
    }

    @Test
    void testAdd() {

        prepareSecurityContext();

        Book addBook = new Book(null, "新宿鮫", "大沢在昌");

        Book actual = client
                .post()
                .uri("/books")
                .body(addBook)
                .retrieve()
                .body(Book.class);

        assertThat(actual.getId()).isEqualTo(4);
    }

    @Test
    void testUpdate() {

        prepareSecurityContext();

        Book updateBook = expectedBook1.copy();
        updateBook.setTitle("update-title");
        updateBook.setAuthor("update-author");

        Book actual = client
                .put()
                .uri("/books")
                .body(updateBook)
                .retrieve()
                .body(Book.class);

        assertThat(actual.getId()).isEqualTo(1);
        assertThat(actual.getTitle()).isEqualTo("update-title");
        assertThat(actual.getAuthor()).isEqualTo("update-author");
    }

    @Test
    void testDelete() {

        prepareSecurityContext();

        client.delete()
                .uri("/books/{id}", 1)
                .retrieve()
                .toBodilessEntity();
    }

    @Test
    void testUpload() {

        prepareSecurityContext();

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new ClassPathResource("mz-tech-logo-small.png"));

        String actual = client
                .post()
                .uri("/books/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .body(String.class);

        assertThat(actual).isEqualTo("mz-tech-logo-small.png");
    }

    @Test
    void testDownload() {

        prepareSecurityContext();

        Resource actual = client
                .get()
                .uri("/books/files/{filename}", "mz-tech-logo-small.png")
                .retrieve()
                .body(Resource.class);

        assertThat(actual).isNotNull();
    }

    private void prepareSecurityContext() {
        Authentication auth = new TestingAuthenticationToken("ID0001", "test", "MEMBER");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}