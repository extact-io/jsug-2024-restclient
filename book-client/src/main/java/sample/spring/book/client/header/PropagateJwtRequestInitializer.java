package sample.spring.book.client.header;

import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestInitializer;

public class PropagateJwtRequestInitializer implements ClientHttpRequestInitializer {

    @Override
    public void initialize(ClientHttpRequest request) {

        String rawToken = "...."; // 保管していたものをどこかから取得する
        request.getHeaders().setBearerAuth(rawToken);
    }
}
