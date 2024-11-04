package sample.spring.book.util;

import org.springframework.core.env.Environment;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

public class LocalHostUriBuilderFactory extends DefaultUriBuilderFactory {

    private Environment env;
    private String basePath;

    public LocalHostUriBuilderFactory(Environment env) {
        this(env, "");
    }

    public LocalHostUriBuilderFactory(Environment env, String basePath) {
        this.env = env;
        this.basePath = basePath;
    }

    // UriBuilderFactory

    @Override
    public UriBuilder uriString(String uriTemplate) {
        return super.uriString(localhostUriTemplate() + uriTemplate);
    }

    @Override
    public UriBuilder builder() {
        return super.uriString(localhostUriTemplate());
    }

    private String localhostUriTemplate() {
        return "http://localhost:" + env.getProperty("local.server.port") + basePath;
    }
}
