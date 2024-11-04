package sample.spring.book.stub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookServerModel {

    @NotNull(groups = Update.class)
    private Integer id;
    @NotBlank
    @Size(min = 1, max = 20)
    private String title;
    @Size(max = 20)
    private String author;
    private String published;

    public boolean hasSameTitle(BookServerModel other) {
        if (other == null) {
            return false;
        }
        if (this == other || this.getId().equals(other.getId())) {
            return false;
        }
        return this.title.equals(other.getTitle());
    }

    public BookServerModel copy() {
        return new BookServerModel(id, title, author, published);
    }

    public interface Update {
    }
}
