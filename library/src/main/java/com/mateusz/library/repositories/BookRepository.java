package com.mateusz.library.repositories;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;
import com.mateusz.library.model.dao.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {

    List<BookEntity> findByTitle (String title);

    List<BookEntity> findByPresentIsFalse();

    List<BookEntity> findByTitleAndPresentIsTrue(String title);
    List<BookEntity> findByCategoriesList_name(String categoryName);

    List<BookEntity> findByCategoriesList(CategoryEntity categoryEntity);

    List<BookEntity> findByTitleContainingAndAuthorAndCategoriesList_name (String title, String author, String[] listOfCategories);

    List<BookEntity> findByTitleContaining(String title);
    List<BookEntity> findByAuthor(String author);

    List<BookEntity> findByPrice(BigDecimal price);

    Optional<BookEntity> findByCurrentUserAndTitle(UserEntity user, String title);

}
