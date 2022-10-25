package com.mateusz.library.services;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;
import com.mateusz.library.repositories.BookRepository;
import com.mateusz.library.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

public final CategoryRepository categoryRepository;

public final BookRepository bookRepository;


    public CategoryEntity deleteCategoryById(Long categoryId) {
        CategoryEntity categoryToRemove = categoryRepository.findById(categoryId).orElseThrow(() -> new NoResultException("category not found"));
        return deleteCategory(categoryToRemove);
//        List<BookEntity> listOfBooksContainingCategory = bookRepository.findByCategoriesList(categoryToRemove);
//        for (BookEntity bookEntity : listOfBooksContainingCategory){
//            bookEntity.removeCategory(categoryToRemove);
//        }
//        categoryRepository.delete(categoryToRemove);
//        return null;
    }

    public CategoryEntity deleteCategoryByName(String categoryName) {
        CategoryEntity categoryToRemove = categoryRepository.findCategoryByName(categoryName).orElseThrow(() -> new NoResultException("category not found"));
        return deleteCategory(categoryToRemove);
    }


    private CategoryEntity deleteCategory (CategoryEntity categoryToRemove) {
        List<BookEntity> listOfBooksContainingCategory = bookRepository.findByCategoriesList(categoryToRemove);
        for (BookEntity bookEntity : listOfBooksContainingCategory){
            bookEntity.removeCategory(categoryToRemove);
        }
        categoryRepository.delete(categoryToRemove);
        return null;
    }
}
