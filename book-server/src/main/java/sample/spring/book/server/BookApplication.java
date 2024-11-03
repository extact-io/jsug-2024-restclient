package sample.spring.book.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

import ch.qos.logback.access.tomcat.LogbackValve;

@SpringBootApplication
public class BookApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookApplication.class, args);
    }

    @Bean
    TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory();
        LogbackValve valve = new LogbackValve();
        valve.setFilename(LogbackValve.DEFAULT_FILENAME);
        tomcatServletWebServerFactory.addContextValves(valve);
        return tomcatServletWebServerFactory;
    }
}
