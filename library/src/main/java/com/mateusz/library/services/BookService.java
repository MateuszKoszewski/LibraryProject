package com.mateusz.library.services;

import com.mateusz.library.model.dto.GetBookResponse;
import com.mateusz.library.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

private final BookRepository bookRepository;

    public List<GetBookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(book -> GetBookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .build())
                .collect(Collectors.toList());
    }
}
