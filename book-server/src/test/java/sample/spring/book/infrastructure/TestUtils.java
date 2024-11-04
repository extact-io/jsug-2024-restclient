package sample.spring.book.infrastructure;

import java.time.LocalDate;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import sample.spring.book.infrastructure.component.CustomLocalDateDeserializer;
import sample.spring.book.infrastructure.component.CustomLocalDateSerializer;

public class TestUtils {

    static MappingJackson2HttpMessageConverter customJsonMessageConveter() {

        String localDatePattern = "yyyy/MM/dd";

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new CustomLocalDateSerializer(localDatePattern));
        module.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer(localDatePattern));
        mapper.registerModule(module);

        return new MappingJackson2HttpMessageConverter(mapper);
    }
}
