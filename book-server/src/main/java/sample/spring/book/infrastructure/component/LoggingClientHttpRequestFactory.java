package sample.spring.book.infrastructure.component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
public class LoggingClientHttpRequestFactory implements ClientHttpRequestFactory {

    private final ClientHttpRequestFactory originalFactory;

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        ClientHttpRequest delegate = originalFactory.createRequest(uri, httpMethod);
        return new LoggingClientHttpRequest(delegate);
    }


    @RequiredArgsConstructor
    @Slf4j
    static class LoggingClientHttpRequest implements ClientHttpRequest {

        private final ClientHttpRequest delegate;

        @Override
        public HttpMethod getMethod() {
            return delegate.getMethod();
        }

        @Override
        public URI getURI() {
            return delegate.getURI();
        }

        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public OutputStream getBody() throws IOException {
            return delegate.getBody();
        }

        @Override
        public ClientHttpResponse execute() throws IOException {
            logRequestHeaders(delegate);
            ClientHttpResponse response = delegate.execute();
            logResponseHeaders(response);
            return response;
        }

        private void logRequestHeaders(HttpRequest request) {

            // ClientHttpRequest#getBodyはメモリ効率とStreamの復元も含め慎重に行う必要があるため省略。
            // 解決方法の1つとして通信路に実際に出力するOutputStreamのStreamをキャプチャしながらログ
            // にも書く方法がある。

            log.info("Request ================================================================");
            log.info("Request URI: " + request.getURI());
            log.info("Request Method: " + request.getMethod());
            log.info("Request Headers: " + request.getHeaders());
            log.info("Request ================================================================");
        }

        private void logResponseHeaders(ClientHttpResponse response) throws IOException {

            // LoggingInterceptorのClientHttpResponseと同じ理由でボディの出力は省略。
            // 解決方法の1つとしてInputStream#markで開始位置をマークし制御を戻すに前にInputStream#resetで
            // 進んだストリーム位置を戻す方法がある

            log.info("Response ================================================================");
            log.info("Response Status Code: " + response.getStatusCode());
            log.info("Response Headers: " + response.getHeaders());
            log.info("Response ================================================================");

        }
    }
}
