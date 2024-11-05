package sample.spring.book.infrastructure;

import java.time.LocalDate;

public record AddRequest(
        String title,
        String author,
        LocalDate published) {
}
