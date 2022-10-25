package com.mateusz.library.repositories;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {

    BookEntity findByTitle (String title);
    List<BookEntity> findByCategoriesList_name (String categoryName);

    List<BookEntity> findByCategoriesList (CategoryEntity categoryEntity);
}
