package sample.spring.book.infrastructure.component.jackson;

import java.time.LocalDate;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class CustomMessageConveterFactory {

    public MappingJackson2HttpMessageConverter create(String pattern) {

        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();

        module.addSerializer(LocalDate.class, new ConfigurableLocalDateSerializer(pattern));
        module.addDeserializer(LocalDate.class, new ConfigurableLocalDateDeserializer(pattern));

        mapper.registerModule(module);

        return new MappingJackson2HttpMessageConverter(mapper);
    }
}
