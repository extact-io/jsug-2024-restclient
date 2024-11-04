package sample.spring.book.domain;

import static org.assertj.core.api.Assertions.*;
import static sample.spring.book.junit.EnabledIfClientType.ClientType.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import sample.spring.book.exception.DuplicateException;
import sample.spring.book.exception.NotFoundException;
import sample.spring.book.exception.ValidationException;
import sample.spring.book.junit.EnabledIfClientType;
import sample.spring.book.stub.BookApplication;
import sample.spring.book.stub.BookRepository;
import sample.spring.book.stub.impl.InMemoryBookRepository;

public abstract class BookClientTest {

    private BookClient client;

    private final Book expectedBook1 = new Book(1, "燃えよ剣", "司馬遼太郎", LocalDate.of(1972, 6, 1));
    private final Book expectedBook2 = new Book(2, "峠", "司馬遼太郎", LocalDate.of(1968, 10, 1));
    private final Book expectedBook3 = new Book(3, "ノルウェイの森", "村上春樹", LocalDate.of(1987, 9, 4));

    @Configuration(proxyBeanMethods = false)
    @Import(BookApplication.class)
    public static class TestConfig {

        @Bean
        @Scope("prototype")
        @Primary
        BookRepository bookRepository() {
            return new InMemoryBookRepository();
        }
    }

    protected abstract BookClient retrieveTestInstanceBeforeEach(int port);

    @BeforeEach
    void beforeEach(@Value("${local.server.port}") int port) {

        SecurityContextHolder.clearContext();

        this.client = retrieveTestInstanceBeforeEach(port);
    }

    @Test
    @EnabledIfClientType(All)
    void testGet() {

        Optional<Book> actual = client.get(1);
        assertThat(actual).isPresent().contains(expectedBook1);

        actual = client.get(999);
        assertThat(actual).isEmpty();
    }

    @Test
    @EnabledIfClientType(All)
    void testGetAll() {

        List<Book> actual = client.getAll();
        assertThat(actual).containsExactly(expectedBook1, expectedBook2, expectedBook3);
    }

    @Test
    @EnabledIfClientType(All)
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
    @EnabledIfClientType(All)
    void testFindByAuthorStartingWith() {

        List<Book> actual = client.findByAuthorStartingWith("司馬");
        assertThat(actual).containsExactly(expectedBook1, expectedBook2);

        actual = client.findByAuthorStartingWith("unknown");
        assertThat(actual).isEmpty();
    }

    @Test
    @EnabledIfClientType(All)
    void testAdd() {

        prepareSecurityContext();

        Book actual = client.add("新宿鮫", "大沢在昌", LocalDate.of(1990, 1, 1));

        assertThat(actual.getId()).isEqualTo(4);
    }

    @Test
    @EnabledIfClientType(All)
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
    @EnabledIfClientType(All)
    void testDelete() {

        prepareSecurityContext();

        client.delete(1);
    }

    @Test
    @EnabledIfClientType({ RestClient, HTTPInterface })
    void testUpload() {

        prepareSecurityContext();

        String actual = client.upload("mz-tech-logo-small.png");

        assertThat(actual).isEqualTo("mz-tech-logo-small.png");
    }

    @Test
    @EnabledIfClientType({ RestClient, HTTPInterface })
    void testDownload() {

        prepareSecurityContext();

        Resource actual = client.download("mz-tech-logo-small.png");

        assertThat(actual).isNotNull();
    }


    // ----------------------------------------------------- Exception Testing

    @Test
    @EnabledIfClientType({ RestClient, HTTPInterface })
    void testAddOccurValidationError() {

        prepareSecurityContext();

        assertThatThrownBy(() -> client.add(null, null, null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @EnabledIfClientType({ RestClient, HTTPInterface })
    void testUpdateOccurValidationError() {

        prepareSecurityContext();

        assertThatThrownBy(() -> client.update(new Book(999, null, null, null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @EnabledIfClientType({ RestClient, HTTPInterface })
    void testAddOccurDuplicateException() {

        prepareSecurityContext();

        assertThatThrownBy(() -> client.add("峠", "司馬遼太郎", null))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @EnabledIfClientType({ RestClient, HTTPInterface })
    void testUpdateOccurNotFoundException() {

        prepareSecurityContext();

        Book updateBook = new Book(999, "新宿鮫", "大沢在昌", LocalDate.of(1990, 1, 1));
        assertThatThrownBy(() -> client.update(updateBook))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @EnabledIfClientType({ RestClient, HTTPInterface })
    void testDeleteOccurNotFoundException() {

        prepareSecurityContext();

        assertThatThrownBy(() -> client.delete(999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @EnabledIfClientType({ RestClient, HTTPInterface })
    void testUpdateOccurConstrainException() {

        prepareSecurityContext();

        assertThatThrownBy(() -> client.update(new Book(3, "燃えよ剣", null, null)))
                .isInstanceOf(DuplicateException.class);
    }


    // ----------------------------------------------------- private methods

    private void prepareSecurityContext() {
        Authentication auth = new TestingAuthenticationToken("ID0001", "test", "MEMBER");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


}