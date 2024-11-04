package sample.spring.book.infrastructure;

import java.time.Duration;
import java.util.Map;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import sample.spring.book.domain.BookClient;
import sample.spring.book.domain.BookClientTest;
import sample.spring.book.infrastructure.component.BookResponseErrorHandler;
import sample.spring.book.infrastructure.component.LoggingInterceptor;
import sample.spring.book.infrastructure.component.PropagateUserContextInitializer;
import sample.spring.book.stub.BookApplication;

@SpringBootTest(classes = BookClientTest.TestConfig.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookClientRestClientAdapterTest extends BookClientTest {

    @Override
    protected BookClient retrieveTestInstanceBeforeEach(int port) {

        //ClientHttpRequestFactory requestFactory = simpleCreateFactory();
        ClientHttpRequestFactory requestFactory = customHttpClientCreateFactory();

        MappingJackson2HttpMessageConverter converter = TestUtils.customJsonMessageConveter();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl("http://localhost:" + port)
                .queryParam("Sender-Name", BookApplication.class.getSimpleName());
        UriBuilderFactory uriFactory = new DefaultUriBuilderFactory(uriBuilder);

        RestClient restClient = RestClient.builder()
                .requestFactory(requestFactory)
                //.baseUrl("http://localhost:" + port)
                .uriBuilderFactory(uriFactory)
                .messageConverters(converters -> converters.addFirst(converter))
                .defaultHeader("Sender-Name", BookClientRestClientAdapter.class.getSimpleName())
                .defaultUriVariables(Map.of("context", "books"))
                .defaultStatusHandler(new BookResponseErrorHandler())
                .requestInitializer(new PropagateUserContextInitializer())
                .requestInterceptor(new LoggingInterceptor())
                .build();

        return new BookClientRestClientAdapter(restClient);
    }

    private ClientHttpRequestFactory simpleCreateFactory() {

        Duration connectTimeout = Duration.ofSeconds(5);
        ClientHttpRequestFactorySettings settings = new ClientHttpRequestFactorySettings(
                connectTimeout,
                null,
                (SslBundle) null);

        return ClientHttpRequestFactories.get(
                HttpComponentsClientHttpRequestFactory::new,
                settings);
    }

    private ClientHttpRequestFactory customHttpClientCreateFactory() {

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(5))   // 接続タイムアウト(default:3sec)
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofSeconds(30)) // 読み取りタイムアウト(default:none)
                .setMaxRedirects(3)                         // 最大リダイレクト回数(default:50)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
