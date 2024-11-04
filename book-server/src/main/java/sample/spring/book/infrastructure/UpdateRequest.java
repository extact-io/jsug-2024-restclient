package sample.spring.book.infrastructure;

import sample.spring.book.domain.Book;

record UpdateRequest(
        int id,
        String title,
        String author) {

    static UpdateRequest from(Book model) {
        return new UpdateRequest(model.getId(), model.getTitle(), model.getAuthor());
    }
}
