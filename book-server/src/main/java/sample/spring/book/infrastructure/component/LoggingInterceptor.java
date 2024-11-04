package sample.spring.book.infrastructure.component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        // リクエストのヘッダーとボディをログ出力
        logRequestDetails(request, body);

        // リクエストを実行し、レスポンスを取得
        ClientHttpResponse response = execution.execute(request, body);

        // レスポンスのヘッダーをログ出力
        logResponseDetails(response);

        return response;
    }

    private void logRequestDetails(HttpRequest request, byte[] body) {

        log.info("Request URI: " + request.getURI());
        log.info("Request Method: " + request.getMethod());
        log.info("Request Headers: " + request.getHeaders());

        // リクエストボディのログ出力（必要な場合）
        if (body.length > 0) {
            log.info("Request Body: " + new String(body, StandardCharsets.UTF_8));
        }
    }

    private void logResponseDetails(ClientHttpResponse response) throws IOException {

        log.info("Response Status Code: " + response.getStatusCode());
        log.info("Response Headers: " + response.getHeaders());

        // response.getBody()をするとInputStreamを読み切ってしまいJSON→Objectするデータが取れなくなる
        // なので、responseのボディをログに出したい場合は読み切ったStreamを戻す必要がある

        // レスポンスボディのログ出力（必要な場合）
        //String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        //if (!responseBody.isEmpty()) {
        //    log.info("Response Body: " + responseBody);
        //}
    }
}
