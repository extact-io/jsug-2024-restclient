package sample.spring.book.infrastructure.component;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;

public class CustomUriBuilder implements UriBuilder {

    private final UriBuilder delegate;
    private final DateTimeFormatter formatter;

    public CustomUriBuilder(UriBuilder original, String localDataPattern) {
        this.delegate = original;
        this.formatter = DateTimeFormatter.ofPattern(localDataPattern);
    }

    public UriBuilder scheme(String scheme) {
        return delegate.scheme(scheme);
    }

    public UriBuilder userInfo(String userInfo) {
        return delegate.userInfo(userInfo);
    }

    public UriBuilder host(String host) {
        return delegate.host(host);
    }

    public UriBuilder port(int port) {
        return delegate.port(port);
    }

    public UriBuilder port(String port) {
        return delegate.port(port);
    }

    public UriBuilder path(String path) {
        return delegate.path(path);
    }

    public UriBuilder replacePath(String path) {
        return delegate.replacePath(path);
    }

    public UriBuilder pathSegment(String... pathSegments) throws IllegalArgumentException {
        return delegate.pathSegment(pathSegments);
    }

    public UriBuilder query(String query) {
        return delegate.query(query);
    }

    public UriBuilder replaceQuery(String query) {
        return delegate.replaceQuery(query);
    }

    public UriBuilder queryParam(String name, Object... values) {
        // 特定の型だけはこの時点で文字列化して実体のUriBuilderに渡す
        List<Object> convetedValues = Stream.of(values)
                .map(this::convertValue)
                .toList();
        return delegate.queryParam(name, convetedValues);
    }

    public UriBuilder queryParam(String name, Collection<?> values) {
        List<Object> convetedValues = values.stream()
                .map(this::convertValue)
                .toList();
        return delegate.queryParam(name, convetedValues);
    }

    public UriBuilder queryParamIfPresent(String name, Optional<?> value) {
        value.map(this::convertValue)
                .ifPresent(v -> delegate.queryParam(name, v));
        return delegate;
    }

    public UriBuilder queryParams(MultiValueMap<String, String> params) {
        return delegate.queryParams(params);
    }

    public UriBuilder replaceQueryParam(String name, Object... values) {
        List<Object> convetedValues = Stream.of(values)
                .map(this::convertValue)
                .toList();
        return delegate.replaceQueryParam(name, convetedValues);
    }

    public UriBuilder replaceQueryParam(String name, Collection<?> values) {
        List<Object> convetedValues = values.stream()
                .map(this::convertValue)
                .toList();
        return delegate.replaceQueryParam(name, convetedValues);
    }

    public UriBuilder replaceQueryParams(MultiValueMap<String, String> params) {
        return delegate.replaceQueryParams(params);
    }

    public UriBuilder fragment(String fragment) {
        return delegate.fragment(fragment);
    }

    // パスパラメータの変換はこのメソッドで行われる
    public URI build(Object... uriVariables) {
        Object[] convetedValues = Stream.of(uriVariables)
                .map(this::convertValue)
                .toArray();
        return delegate.build(convetedValues);
    }

    public URI build(Map<String, ?> uriVariables) {
        return delegate.build(uriVariables);
    }

    public String toUriString() {
        return delegate.toUriString();
    }

    private Object convertValue(Object value) {
        return switch (value) {
            case LocalDate ld -> ld.format(formatter);
            default -> value;
        };
    }
}
