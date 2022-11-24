package com.mateusz.library.services;

import com.mateusz.library.TestUtils;
import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.repositories.*;
import com.mateusz.library.utils.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.NoResultException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private HistoryOfBooksRepository historyOfBooksRepository;

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
    @Test
    void shouldGetBookByTitle() {
        //
        //Given
        //
        BookEntity bookEntity = TestUtils.createSimpleBook();
        //
        //When
        //
        Mockito.when(bookRepository.findByTitle(bookEntity.getTitle())).thenReturn(List.of(bookEntity));
        List<BookEntity> foundedBooks = bookService.getBookByTitle(bookEntity.getTitle());
        //
        //Then
        //
        assertEquals(List.of(bookEntity), foundedBooks);
    }
    @Test
    void shouldNotGetBookByTitle_bookNotExist() {
        assertThrows(NoResultException.class, () -> bookService.getBookByTitle(TestUtils.createSimpleBook().getTitle()));
    }
    @Test
    void shouldSearchBookByAuthorByTitleByPriceByCategories() {
        //
        //Given
        //
        BookEntity bookEntity = TestUtils.createSimpleBook();
        //
        //When
        //
        Mockito.when(bookRepository.findByTitleContaining(bookEntity.getTitle())).thenReturn(List.of(bookEntity));
        Mockito.when(bookRepository.findByAuthor(bookEntity.getAuthor())).thenReturn(List.of(bookEntity));
        Mockito.when(bookRepository.findByPrice(bookEntity.getPrice())).thenReturn(List.of(bookEntity));
        createMockitoConditionsForSearchingByMultipleCategories(bookEntity);
        List<BookEntity> foundedBooks = bookService.searchBook(bookEntity);
        //
        //Then
        //
        assertEquals(List.of(bookEntity), foundedBooks);
    }
    @Test
    void shouldNotSearchBook_noResults() {
        assertThrows(NoResultException.class, () -> bookService.searchBook(TestUtils.createSimpleBook()));
    }
@Test
    void shouldRentBook() {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        BookEntity bookEntity = TestUtils.createSimpleBook();
        //
        //When
        //
        TestUtils.authenticateUser(userEntity);
        Mockito.when(bookRepository.findByTitleAndPresentIsTrue(bookEntity.getTitle())).thenReturn(List.of(bookEntity));
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        BookEntity rentedBook = bookService.rentBook(bookEntity.getTitle());
        //
        //Then
        //
        assertEquals(bookEntity, rentedBook);
        assertEquals(bookEntity, userEntity.getRentedBooks().get(0));
        assertEquals(userEntity, userEntity.getRentedBooks().get(0).getCurrentUser());
        assertEquals(1, userEntity.getNotifications().size());
        assertEquals(DateUtils.parseDateToLocalDate(new Date()), bookEntity.getDateOfRent());
    }
@Test
    void shouldNotRentBook_noResult() {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        //
        //When
        //
        TestUtils.authenticateUser(userEntity);
        //
        //Then
        //
        assertThrows(NoResultException.class, () -> bookService.rentBook(TestUtils.createSimpleBook().getTitle()));
    }
    @Test
    void shouldReturnBook() {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        BookEntity bookEntity = TestUtils.createSimpleBook();
        bookEntity.setPresent(false);
        bookEntity.setCurrentUser(userEntity);
        //
        //When
        //
        TestUtils.authenticateUser(userEntity);
        Mockito.when(bookRepository.findByCurrentUserAndTitle(userEntity, bookEntity.getTitle())).thenReturn(Optional.of(bookEntity));
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        BookEntity returnedBook = bookService.returnBook(bookEntity.getTitle());
        //
        //Then
        //
        assertEquals(bookEntity, returnedBook);
        assertTrue(returnedBook.isPresent());
        assertNull(returnedBook.getCurrentUser());
        assertEquals(1, userEntity.getNotifications().size());
    }
    @Test
    void shouldGetBookOwnerByBookId() {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        BookEntity bookEntity = TestUtils.createSimpleBook();
        bookEntity.setCurrentUser(userEntity);
        //
        //When
        //
        Mockito.when(bookRepository.findById(bookEntity.getId())).thenReturn(Optional.of(bookEntity));
        UserEntity userLookedFor = bookService.getBookOwnerByBookId(bookEntity.getId());
        //
        //Then
        //
        assertEquals(userEntity, userLookedFor);
    }

    private void createMockitoConditionsForSearchingByMultipleCategories(BookEntity bookEntity) {
        for (CategoryEntity category : bookEntity.getCategoriesList()) {
            Mockito.when(bookRepository.findByCategoriesList_name(category.getName())).thenReturn(List.of(bookEntity));
        }
    }

}
