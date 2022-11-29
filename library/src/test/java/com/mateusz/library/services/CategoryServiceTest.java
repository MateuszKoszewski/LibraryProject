package com.mateusz.library.services;

import com.mateusz.library.TestUtils;
import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;
import com.mateusz.library.repositories.BookRepository;
import com.mateusz.library.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookRepository bookRepository;

    @Test
    void shouldDeleteCategoryById() {
        //
        //Given
        //
        BookEntity bookEntity = TestUtils.createSimpleBook();
        CategoryEntity categoryEntity = bookEntity.getCategoriesList().get(0);
        //
        //When
        //
        Mockito.when(bookRepository.findByCategoriesList(categoryEntity)).thenReturn(List.of(bookEntity));
        Mockito.when(categoryRepository.findById(categoryEntity.getId())).thenReturn(Optional.of(categoryEntity));
        CategoryEntity removedCategory = categoryService.deleteCategoryById(categoryEntity.getId());
        //
        //Then
        //
        assertEquals(categoryEntity, removedCategory);
        Mockito.verify(categoryRepository).delete(categoryEntity);
    }

    @Test
    void shouldDeleteCategoryByName() {
        //
        //Given
        //
        BookEntity bookEntity = TestUtils.createSimpleBook();
        CategoryEntity categoryEntity = bookEntity.getCategoriesList().get(0);
        //
        //When
        //
        Mockito.when(bookRepository.findByCategoriesList(categoryEntity)).thenReturn(List.of(bookEntity));
        Mockito.when(categoryRepository.findCategoryByName(categoryEntity.getName())).thenReturn(Optional.of(categoryEntity));
        CategoryEntity removedCategory = categoryService.deleteCategoryByName(categoryEntity.getName());
        //
        //Then
        //
        assertEquals(categoryEntity, removedCategory);
        Mockito.verify(categoryRepository).delete(categoryEntity);
    }

}
