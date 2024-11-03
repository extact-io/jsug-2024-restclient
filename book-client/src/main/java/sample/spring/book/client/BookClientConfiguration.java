package sample.spring.book.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import sample.spring.book.client.exception.BookResponseErrorHandler;

@Configuration(proxyBeanMethods = false)
public class BookClientConfiguration {

    @Bean
    BookClient bookClient(@Value("${target.url}") String url) {

        RestClient restClient = RestClient.builder()
                .baseUrl(url)
                //.requestInitializer(new PropagateJwtRequestInitializer())
                .defaultStatusHandler(new BookResponseErrorHandler())
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        return factory.createClient(BookClient.class);
    }
}
