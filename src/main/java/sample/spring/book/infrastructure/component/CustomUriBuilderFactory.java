package sample.spring.book.infrastructure.component;

import org.springframework.core.env.Environment;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

public class CustomUriBuilderFactory extends DefaultUriBuilderFactory {

    private static final String DEFAULT_LOCAL_DATE_PATTERN = "yyyy-MM-dd";

    private final Environment env;
    private final String uriTemplate;
    private final String localDatePattern;

    public CustomUriBuilderFactory(Environment env) {
        this(env, "", DEFAULT_LOCAL_DATE_PATTERN);
    }

    public CustomUriBuilderFactory(Environment env, String uriTemplate) {
        this(env, uriTemplate, DEFAULT_LOCAL_DATE_PATTERN);
    }

    public CustomUriBuilderFactory(Environment env, String uriTemplate, String localDataPattern) {
        this.env = env;
        this.uriTemplate= uriTemplate;
        this.localDatePattern = localDataPattern;
    }

    @Override
    public UriBuilder uriString(String uriTemplate) {
        String baseUriTemplate = resolveTemplate() + uriTemplate;
        UriBuilder original = super.uriString(baseUriTemplate);
        return new CustomUriBuilder(original, localDatePattern);
    }

    @Override
    public UriBuilder builder() {
        return this.uriString(resolveTemplate());
    }

    private String resolveTemplate() {
        return env.resolveRequiredPlaceholders(this.uriTemplate);
    }
}
