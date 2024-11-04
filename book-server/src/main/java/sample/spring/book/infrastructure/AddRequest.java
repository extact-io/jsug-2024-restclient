package sample.spring.book.infrastructure;

import java.time.LocalDate;

record AddRequest(
        String title,
        String author,
        LocalDate published) {
}
