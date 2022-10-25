package com.mateusz.library.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.engine.internal.Cascade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "categoriesList")
    private List<BookEntity> listOfBooks;

    public void addBook(BookEntity bookEntity) {
        this.listOfBooks.add(bookEntity);
        bookEntity.getCategoriesList().add(this);
    }

    public void removeBook(BookEntity bookEntity) {
        this.listOfBooks.remove(bookEntity);
        bookEntity.getCategoriesList().remove(this);
    }

    public void removeBooks() {
        this.listOfBooks = new ArrayList<>();
    }
}
