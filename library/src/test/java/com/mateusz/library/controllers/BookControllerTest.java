package com.mateusz.library.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mateusz.library.TestUtils;
import com.mateusz.library.configuration.TestConfig;
import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.AddBookRequest;
import com.mateusz.library.security.JWTTokenProvider;
import com.mateusz.library.services.BookService;
import com.mateusz.library.services.UserService;
import com.mateusz.library.utils.LibraryStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(BookController.class)
@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserService userService;

    @Test
    void shouldGetAllRentedBooks() throws Exception {
        //
        //Given
        //
        UserEntity loggedInUser = TestUtils.getAdmin();
        List<BookEntity> listOfBooks = List.of(TestUtils.createSimpleBook());
        //
        //When
        //
        Mockito.when(bookService.getAllRentedBooks()).thenReturn(listOfBooks);
        MvcResult result = TestUtils.getResultByRequestBody(mockMvc, objectMapper, jwtTokenProvider, HttpMethod.GET, "/api/book/getAllRentedBooks", loggedInUser, listOfBooks);
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    void shouldNotGetAllRentedBooks_wrongUserAuthenticated() throws Exception {
        //
        //Given
        //
        UserEntity loggedInUser = TestUtils.getSimpleUser();
        List<BookEntity> listOfBooks = List.of(TestUtils.createSimpleBook());
        //
        //When
        //
        Mockito.when(bookService.getAllRentedBooks()).thenReturn(listOfBooks);
        MvcResult result = TestUtils.getResultByRequestBody(mockMvc, objectMapper, jwtTokenProvider, HttpMethod.GET, "/api/book/getAllRentedBooks", loggedInUser, listOfBooks);
        //
        //Then
        //
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    void shouldAddBook() throws Exception {
        //
        //Given
        //
        UserEntity loggedInUser = TestUtils.getAdmin();
        BookEntity bookEntity = TestUtils.createSimpleBook();
        AddBookRequest addBookRequest = createAddBookRequestForBookEntity(bookEntity);
        String[] categoriesList = LibraryStringUtils.getCategoriesFromBookEntity(bookEntity);
        for (CategoryEntity category: bookEntity.getCategoriesList()) {
            category.setListOfBooks(null);
        }
        //
        //When
        //
        Mockito.when(bookService.addBook(bookEntity.getTitle(), bookEntity.getAuthor(), categoriesList, bookEntity.getPrice())).thenReturn(bookEntity);
        MvcResult result = TestUtils.getResultByRequestBody(mockMvc, objectMapper, jwtTokenProvider, HttpMethod.POST, "/api/book/addBook", loggedInUser, addBookRequest);
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(bookEntity), result.getResponse().getContentAsString());
    }

    private AddBookRequest createAddBookRequestForBookEntity(BookEntity bookEntity) {
        AddBookRequest addBookRequest = new AddBookRequest();
        addBookRequest.setAuthor(bookEntity.getAuthor());
        addBookRequest.setTitle(bookEntity.getTitle());
        addBookRequest.setCategoriesList(LibraryStringUtils.getCategoriesFromBookEntity(bookEntity));
        addBookRequest.setPrice(bookEntity.getPrice());
        return addBookRequest;
    }
}
