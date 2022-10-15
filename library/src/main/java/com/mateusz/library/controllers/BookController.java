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
public class BookController {

    private final BookService bookService;

    @GetMapping("/api/getAllBooks")
    public List<GetBookResponse> getAllBooks() {
        return bookService.getAllBooks();
    }

    @PostMapping("/api/book")
    public AddBookResponse addBook(@RequestBody AddBookRequest addBookRequest) {
        return bookService.addBook(addBookRequest);
    }
@PostMapping("/api/rentBook")
    public AddBookResponse rentBook(@RequestParam("userEmail") String userEmail, @RequestParam("bookTitle") String bookTitle){
        return bookService.rentBook(userEmail, bookTitle);
    }

}
