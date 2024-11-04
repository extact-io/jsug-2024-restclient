package sample.spring.book.infrastructure;

import java.time.LocalDate;

import sample.spring.book.domain.Book;

record BookResponse(
        int id,
        String title,
        String author,
        LocalDate published) {

    Book toModel() {
        return new Book(id, title, author, published);
    }
}
