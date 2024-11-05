package sample.spring.book.support;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import sample.spring.book.infrastructure.BookClientHttpInterfaceAdapterTest;
import sample.spring.book.infrastructure.BookClientRestClientAdapterTest;
import sample.spring.book.infrastructure.BookClientRestTemplateAdapterTest;
import sample.spring.book.support.EnabledIfClientType.ClientType;

public class EnabledIfClientTypeCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {

        Optional<EnabledIfClientType> anno = context.getElement()
                .flatMap(el -> Optional.ofNullable(el.getAnnotation(EnabledIfClientType.class)));

        ClientType[] types = anno.get().value();
        Class<?> testClass = context.getRequiredTestClass();

        boolean match = Stream.of(types)
                .filter(type -> this.match(type, testClass))
                .findAny()
                .isPresent();

        if (match) {
            return ConditionEvaluationResult.enabled("Run test because test class is " + testClass.getSimpleName());
        } else {
            return ConditionEvaluationResult.disabled("Skip test because test class is " + testClass.getSimpleName());
        }
    }

    private boolean match(ClientType type, Class<?> executingTestClass) {
        return switch (type) {
            case ClientType.All -> true;
            case ClientType.RestTemplate -> executingTestClass == BookClientRestTemplateAdapterTest.class;
            case ClientType.RestClient -> executingTestClass == BookClientRestClientAdapterTest.class;
            case ClientType.HTTPInterface -> executingTestClass == BookClientHttpInterfaceAdapterTest.class;
        };

    }
}
