package com.mateusz.library.controllers;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dto.GetBookResponse;
import com.mateusz.library.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/api/getAllBooks")
    public List<GetBookResponse> getAllBooks() {
        return bookService.getAllBooks();
    }

}
