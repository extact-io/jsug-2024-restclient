package sample.spring.book.server;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Book {

    @NotNull(groups = Update.class)
    private Integer id;
    @NotBlank
    @Size(min = 1, max = 20)
    private String title;
    @Size(max = 20)
    private String author;

    public boolean hasSameTitle(Book other) {
        if (other == null) {
            return false;
        }
        if (this == other || this.getId().equals(other.getId())) {
            return false;
        }
        return this.title.equals(other.getTitle());
    }

    public Book copy() {
        return new Book(id, title, author);
    }

    public interface Update {
    }
}
