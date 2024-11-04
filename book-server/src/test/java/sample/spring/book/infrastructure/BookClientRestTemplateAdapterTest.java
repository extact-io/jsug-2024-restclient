package sample.spring.book.infrastructure;

import java.util.List;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import sample.spring.book.domain.BookClient;
import sample.spring.book.domain.BookClientTest;
import sample.spring.book.infrastructure.component.PropagateUserContextInitializer;
import sample.spring.book.infrastructure.component.jackson.CustomMessageConveterFactory;

@SpringBootTest(classes = BookClientTest.TestConfig.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookClientRestTemplateAdapterTest extends BookClientTest {

    @Override
    protected BookClient retrieveTestInstanceBeforeEach(int port) {

        String baseUrl = "http://localhost:" + port;
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(baseUrl);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(uriFactory);
        restTemplate.setClientHttpRequestInitializers(List.of(new PropagateUserContextInitializer()));

        MappingJackson2HttpMessageConverter converter = new CustomMessageConveterFactory().create(LOCAL_DATE_PATTERN);
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.addFirst(converter);
        restTemplate.setMessageConverters(messageConverters);

        return new BookClientRestTemplateAdapter(restTemplate);
    }
}
