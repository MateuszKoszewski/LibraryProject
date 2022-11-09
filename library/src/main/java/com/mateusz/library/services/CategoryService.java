package com.mateusz.library.services;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;
import com.mateusz.library.repositories.BookRepository;
import com.mateusz.library.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

private final CategoryRepository categoryRepository;

private final BookRepository bookRepository;

    @PreAuthorize("hasAuthority('user:delete')")
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
    @PreAuthorize("hasAuthority('user:delete')")
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
