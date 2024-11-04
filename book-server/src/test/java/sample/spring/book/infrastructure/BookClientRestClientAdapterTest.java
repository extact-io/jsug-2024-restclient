package sample.spring.book.infrastructure;

import java.util.Map;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import sample.spring.book.domain.BookClient;
import sample.spring.book.domain.BookClientTest;
import sample.spring.book.infrastructure.component.BookResponseErrorHandler;
import sample.spring.book.infrastructure.component.PropagateUserContextInitializer;
import sample.spring.book.stub.BookApplication;

@SpringBootTest(classes = BookClientTest.TestConfig.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookClientRestClientAdapterTest extends BookClientTest {

    @Override
    protected BookClient retrieveTestInstanceBeforeEach(int port) {

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

        return new BookClientRestClientAdapter(restClient);
    }
}
