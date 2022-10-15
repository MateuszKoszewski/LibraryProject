package com.mateusz.library.services;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.AddBookRequest;
import com.mateusz.library.model.dto.AddBookResponse;
import com.mateusz.library.model.dto.GetBookResponse;
import com.mateusz.library.repositories.BookRepository;
import com.mateusz.library.repositories.UserRepository;
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
private final UserRepository userRepository;

    public List<GetBookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(book -> GetBookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .build())
                .collect(Collectors.toList());
    }

    public AddBookResponse addBook(AddBookRequest addBookRequest){
        BookEntity bookEntity = new BookEntity();
        bookEntity.setTitle(addBookRequest.getTitle());
        bookEntity.setAuthor(addBookRequest.getAuthor());
        bookRepository.save(bookEntity);
        return AddBookResponse.builder().title(addBookRequest.getTitle()).build();
    }

    public AddBookResponse rentBook(String userEmail, String bookTitle) {
        BookEntity bookEntity = bookRepository.findByTitle(bookTitle);
        UserEntity userEntity = userRepository.findByEmail(userEmail);
        userEntity.getRentedBooks().add(bookEntity);
        bookEntity.setUser(userEntity);
        return AddBookResponse.builder().title(bookTitle).build();
    }
}
