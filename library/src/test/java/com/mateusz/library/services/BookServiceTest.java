package com.mateusz.library.services;

import com.mateusz.library.TestUtils;
import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.repositories.BookRepository;
import com.mateusz.library.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void shouldGetAllRentedBooks() {
        //
        //Given
        //
        BookEntity book1 = TestUtils.createSimpleBook();
        book1.setPresent(false);
        BookEntity book2 = TestUtils.createAnotherBook();
        book2.setPresent(false);
        //
        //When
        //
        Mockito.when(bookRepository.findByPresentIsFalse()).thenReturn(List.of(book1, book2));
        List<BookEntity> listOfBooks = bookService.getAllRentedBooks();
        //
        //Then
        //
        assertEquals(listOfBooks, List.of(book1,book2));
    }
    @Test
    void shouldAddBook() {
        //
        //Given
        //
        BookEntity bookEntity = TestUtils.createSimpleBook();
        bookEntity.setId(null);
        //
        //When
        //
        Mockito.when(categoryRepository.findCategoryByName(bookEntity.getCategoriesList().get(0).getName())).thenReturn(Optional.of(bookEntity.getCategoriesList().get(0)));
        BookEntity savedBook = bookService.addBook(bookEntity.getTitle(), bookEntity.getAuthor(), bookEntity.getCategoriesList(), bookEntity.getPrice());
        //
        //Then
        //
        assertEquals(bookEntity, savedBook);
        Mockito.verify(bookRepository).save(bookEntity);
    }
    @Test
    void shouldGetBookById() {
        //
        //Given
        //
        BookEntity bookEntity = TestUtils.createSimpleBook();
        //
        //When
        //
        Mockito.when(bookRepository.findById(bookEntity.getId())).thenReturn(Optional.of(bookEntity));
        BookEntity foundedBook = bookService.getBookById(bookEntity.getId());
        //
        //Then
        //
        assertEquals(bookEntity, foundedBook);
    }
    @Test
    void shouldNotGetBookById_bookNotExist() {
        assertThrows(NoResultException.class, () -> bookService.getBookById(TestUtils.createSimpleBook().getId()));
    }


}
