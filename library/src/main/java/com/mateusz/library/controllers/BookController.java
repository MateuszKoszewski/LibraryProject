package com.mateusz.library.controllers;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dto.AddBookRequest;
import com.mateusz.library.model.dto.AddBookResponse;
import com.mateusz.library.model.dto.GetBookResponse;
import com.mateusz.library.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/book")
public class BookController {

    private final BookService bookService;

    @GetMapping("/getAllBooks")
    public List<GetBookResponse> getAllBooks() {
        return bookService.getAllBooks();
    }

    @PostMapping("/addBook")
    public BookEntity addBook(@RequestBody BookEntity bookEntity) {
        return bookService.addBook(bookEntity.getTitle(), bookEntity.getAuthor(), bookEntity.getCategoriesList(), bookEntity.getPrice());
    }
    @GetMapping("/getById/{bookId}")
    public BookEntity getBookById(@PathVariable(name = "bookId")Long bookId) {
        return bookService.getBookById(bookId);
    }
    @GetMapping("/getByTitle/{bookTitle}")
    public List<BookEntity> getBookByTitle(@PathVariable(name = "bookTitle")String bookTitle) {
        return bookService.getBookByTitle(bookTitle);
    }
    @GetMapping("/search")
    public List<BookEntity> searchBook(@RequestBody BookEntity bookEntity){
        return bookService.searchBook(bookEntity);
    }
@PostMapping("/api/rentBook")
    public AddBookResponse rentBook(@RequestParam("userEmail") String userEmail, @RequestParam("bookTitle") String bookTitle){
        return bookService.rentBook(userEmail, bookTitle);
    }

}
