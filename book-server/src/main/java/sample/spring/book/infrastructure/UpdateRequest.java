package sample.spring.book.infrastructure;

import java.time.LocalDate;

import sample.spring.book.domain.Book;

record UpdateRequest(
        int id,
        String title,
        String author,
        LocalDate published) {

    static UpdateRequest from(Book model) {
        return new UpdateRequest(model.getId(), model.getTitle(), model.getAuthor(), model.getPublished());
    }
}
