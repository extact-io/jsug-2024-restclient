package sample.spring.book.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriBuilderFactory;

import sample.spring.book.domain.BookClient;
import sample.spring.book.domain.BookClientTest;
import sample.spring.book.infrastructure.component.BookResponseErrorHandler;
import sample.spring.book.infrastructure.component.CustomMessageConveterFactory;
import sample.spring.book.infrastructure.component.CustomUriBuilderFactory;
import sample.spring.book.infrastructure.component.LoggingClientHttpRequestFactory;
import sample.spring.book.infrastructure.component.PropagateUserContextInitializer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookClientHttpInterfaceAdapterTest extends BookClientTest {

    @Autowired
    private BookClientApi bookClientApi;

    @Configuration(proxyBeanMethods = false)
    @Import(BookClientTest.TestConfig.class)
    static class HttpInterfaceTestConfig {

        @Bean
        BookClientApi bookClientApi(Environment env) {

            ClientHttpRequestFactory requestFactory = logginClientHttpRequestFactory();
            HttpMessageConverter<Object> converter = customMessageConveter();
            UriBuilderFactory uriFactory = customUriBuilderFactory(env);

            RestClient restClient = RestClient.builder()
                    .requestFactory(requestFactory)
                    .uriBuilderFactory(uriFactory)
                    .messageConverters(converters -> converters.addFirst(converter))
                    .defaultHeader("Sender-Name", BookClientHttpInterfaceAdapter.class.getSimpleName())
                    .defaultStatusHandler(new BookResponseErrorHandler())
                    .requestInitializer(new PropagateUserContextInitializer())
                    .build();

            RestClientAdapter adapter = RestClientAdapter.create(restClient);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
            return factory.createClient(BookClientApi.class);
        }

        private UriBuilderFactory customUriBuilderFactory(Environment env) {
            String baseUriTemplate = "http://localhost:${local.server.port}";
            return new CustomUriBuilderFactory(env, baseUriTemplate, LOCAL_DATE_PATTERN);
        }

        private HttpMessageConverter<Object> customMessageConveter() {
            return new CustomMessageConveterFactory().create(LOCAL_DATE_PATTERN);
        }

        private ClientHttpRequestFactory logginClientHttpRequestFactory() {
            ClientHttpRequestFactory orignal = new HttpComponentsClientHttpRequestFactory();
            return new LoggingClientHttpRequestFactory(orignal);
        }
    }

    @Override
    protected BookClient retrieveTestInstanceBeforeEach(int port) {
        return new BookClientHttpInterfaceAdapter(this.bookClientApi);
    }
}
