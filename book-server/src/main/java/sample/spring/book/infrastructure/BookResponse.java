package sample.spring.book.infrastructure;

import sample.spring.book.domain.Book;

record BookResponse(
        int id,
        String title,
        String author) {

    Book toModel() {
        return new Book(id, title, author);
    }
}
