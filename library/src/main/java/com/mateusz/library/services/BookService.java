package com.mateusz.library.services;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.AddBookRequest;
import com.mateusz.library.model.dto.AddBookResponse;
import com.mateusz.library.model.dto.GetBookResponse;
import com.mateusz.library.repositories.BookRepository;
import com.mateusz.library.repositories.CategoryRepository;
import com.mateusz.library.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

private final BookRepository bookRepository;
private final UserRepository userRepository;

private final CategoryRepository categoryRepository;


    public List<GetBookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(book -> GetBookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .build())
                .collect(Collectors.toList());
    }
@PreAuthorize("hasAuthority('user:delete')")
    public BookEntity addBook(String author, String title, List<CategoryEntity> listOfCategories, BigDecimal price){
        BookEntity bookEntity = new BookEntity();
        bookEntity.setAuthor(author);
        bookEntity.setTitle(title);
        bookEntity.setCategoriesList(setExistingCategories(listOfCategories));
        bookEntity.setPresent(true);
        bookEntity.setPrice(price);
        bookRepository.save(bookEntity);
        return bookEntity;
    }

    private List<CategoryEntity> setExistingCategories(List<CategoryEntity> listOfCategories) {
        List<CategoryEntity> listToSet = new ArrayList<>();
        for (CategoryEntity category : listOfCategories){
            Optional<CategoryEntity> categoryFromDB = categoryRepository.findCategoryByName(category.getName());
            if (categoryFromDB.isPresent()){
                listToSet.add(categoryFromDB.get());
            } else {
                listToSet.add(category);
            }
        }
        return listToSet;
    }

    public AddBookResponse rentBook(String userEmail, String bookTitle) {
        BookEntity bookEntity = bookRepository.findByTitle(bookTitle);
        UserEntity userEntity = userRepository.findUserByEmail(userEmail);
        userEntity.getRentedBooks().add(bookEntity);
        bookEntity.setUser(userEntity);
        return AddBookResponse.builder().title(bookTitle).build();
    }
}
