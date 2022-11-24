package com.mateusz.library.controllers;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.AddBookRequest;
import com.mateusz.library.model.dto.AddBookResponse;
import com.mateusz.library.model.dto.GetBookResponse;
import com.mateusz.library.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/book")
public class BookController {

    private final BookService bookService;

    @PreAuthorize("hasAuthority('user:delete')")
    @GetMapping("/getAllRentedBooks")
    public List<BookEntity> getAllRentedBooks() {
        return bookService.getAllRentedBooks();
    }
    @PreAuthorize("hasAuthority('user:delete')")
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
@PostMapping("/rentBook/{bookTitle}")
    public BookEntity rentBook(@PathVariable(name = "bookTitle") String bookTitle){
        return bookService.rentBook(bookTitle);
    }
    @PreAuthorize("hasAuthority('user:delete')")
@GetMapping("/{bookId}")
    public UserEntity getBookOwnerByBookId(@PathVariable(name = "bookId")Long bookId) {
        return bookService.getBookOwnerByBookId(bookId);
}
@PostMapping("/return/{title}")
public BookEntity returnBook (@PathVariable(name = "title") String title) {
        return bookService.returnBook(title);
}

}
