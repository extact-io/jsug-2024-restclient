package sample.spring.book.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(EnabledIfClientTypeCondition.class)
public @interface EnabledIfClientType {

    ClientType[] value();

    public enum ClientType {
        RestTemplate, //
        RestClient, //
        HTTPInterface, //
        All; //
    }
}
