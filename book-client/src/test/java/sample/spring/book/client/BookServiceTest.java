package sample.spring.book.client;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import sample.spring.book.client.exception.DuplicateClientException;
import sample.spring.book.client.exception.NotFoundClientException;
import sample.spring.book.server.BookApplication;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "spring.main.banner-mode=off")
@TestPropertySource(properties = "server.port=7001")
@TestPropertySource(properties = "target.url=http://localhost:7001")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookServiceTest {

    @Autowired
    private BookService target;

    private final BookDto expectedBook1 = new BookDto(1, "燃えよ剣", "司馬遼太郎");
    private final BookDto expectedBook2 = new BookDto(2, "峠", "司馬遼太郎");

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import({ BookApplication.class, BookClientConfiguration.class })
    static class TestConfig {
        @Bean
        BookService bookService(BookClient bookClient) {
            return new BookService(bookClient);
        }
    }

    @Test
    @Order(1)
    void testGet() {

        BookDto actual = target.get(1);
        assertThat(actual).isEqualTo(expectedBook1);
        actual = target.get(999);
        assertThat(actual).isNull();
    }

    @Test
    @Order(1)
    void testFindByAuthorStartingWith() {

        List<BookDto> actual = target.findByAuthorStartingWith("司馬");
        assertThat(actual).containsExactly(expectedBook1, expectedBook2);

        actual = target.findByAuthorStartingWith("unknown");
        assertThat(actual).isEmpty();
    }

    @Test
    @Order(1)
    void testAddToUpdateToDelete() {

        BookDto addBook = new BookDto(null, "新宿鮫", "大沢在昌");
        BookDto actual = target.add(addBook);
        assertThat(actual.getId()).isEqualTo(4);

        BookDto updateBook = addBook;
        updateBook.setTitle("update-title");
        updateBook.setAuthor("update-author");

        actual = target.update(updateBook);
        assertThat(actual.getTitle()).isEqualTo("update-title");
        assertThat(actual.getAuthor()).isEqualTo("update-author");

        target.delete(actual.getId());
        assertThat(target.get(actual.getId())).isNull();
    }

    @Test
    @Order(9)
    void testAddOccurDuplicateException() {
        BookDto addBook = new BookDto(null, "峠", "司馬遼太郎");
        assertThatThrownBy(() -> target.add(addBook))
                .isInstanceOfSatisfying(
                        DuplicateClientException.class,
                        e -> assertThat(e.getMessage()).contains("message"));
    }

    @Test
    @Order(9)
    void testUpdateOccurNotFoundException() {
        BookDto updateBook = new BookDto(999, "新宿鮫", "大沢在昌");
        assertThatThrownBy(() -> target.update(updateBook))
                .isInstanceOfSatisfying(
                        NotFoundClientException.class,
                        e -> assertThat(e.getMessage()).contains("message"));
    }

    @Test
    @Order(9)
    void testDeleteOccurNotFoundException() {
        assertThatThrownBy(() -> target.delete(999))
                .isInstanceOfSatisfying(
                        NotFoundClientException.class,
                        e -> assertThat(e.getMessage()).contains("message"));
    }
}
