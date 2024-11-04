package sample.spring.book.client.infrastructure;

import sample.spring.book.client.domain.Book;

record BookResponse(
        int id,
        String title,
        String author) {

    Book toModel() {
        return new Book(id, title, author);
    }
}
