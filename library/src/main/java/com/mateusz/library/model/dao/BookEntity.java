package com.mateusz.library.model.dao;

import lombok.Builder;
import lombok.Data;
import org.hibernate.engine.spi.CascadingAction;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "books")
@Data
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String author;

    private boolean present;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
//    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "books_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<CategoryEntity> categoriesList;

    private BigDecimal price;

    private LocalDate dateOfRent;

    private LocalDate dateOfReturn;

    public void addCategory(CategoryEntity categoryEntity) {
        this.categoriesList.add(categoryEntity);
        categoryEntity.getListOfBooks().add(this);
    }

    public void removeCategory(CategoryEntity categoryEntity) {
        this.categoriesList.remove(categoryEntity);
        categoryEntity.getListOfBooks().remove(this);
    }
}
