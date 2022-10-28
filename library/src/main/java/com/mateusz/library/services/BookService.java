package com.mateusz.library.services;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;
import com.mateusz.library.model.dao.HistoryOfBookEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.GetBookResponse;
import com.mateusz.library.repositories.BookRepository;
import com.mateusz.library.repositories.CategoryRepository;
import com.mateusz.library.repositories.HistoryOfBooksRepository;
import com.mateusz.library.repositories.UserRepository;
import com.mateusz.library.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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

private final HistoryOfBooksRepository historyOfBooksRepository;


    public List<GetBookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(book -> GetBookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .build())
                .collect(Collectors.toList());
    }
    @PreAuthorize("hasAuthority('user:delete')")
    public List<BookEntity> getAllRentedBooks() {
        return bookRepository.findByPresentIsFalse();
    }
@PreAuthorize("hasAuthority('user:delete')")
    public BookEntity addBook(String title, String author, List<CategoryEntity> listOfCategories, BigDecimal price){
        BookEntity bookEntity = new BookEntity();
        bookEntity.setAuthor(author.toLowerCase());
        bookEntity.setTitle(title.toLowerCase());
        bookEntity.setCategoriesList(setExistingCategories(listOfCategories));
        bookEntity.setPresent(true);
        bookEntity.setPrice(price);
        bookRepository.save(bookEntity);
        return bookEntity;
    }

    public BookEntity getBookById(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new NoResultException("book doesn't exist"));
    }

    public List<BookEntity> getBookByTitle(String bookTitle) {
        List<BookEntity> listOfBooks = bookRepository.findByTitle(bookTitle);
        if (listOfBooks.isEmpty()) {
            throw new NoResultException("book doesn't exist");
        }
        return listOfBooks;
    }

    public List<BookEntity> searchBook(BookEntity bookEntity) {
        List<BookEntity> listOfResults = new ArrayList<>();
        boolean authorsListIsNull = true;
        boolean titlesListIsNull = true;
        boolean priceListIsNull = true;
        if (StringUtils.isNotBlank(bookEntity.getAuthor())){
            List<BookEntity> listByAuthors = bookRepository.findByAuthor(bookEntity.getAuthor());
            listOfResults.addAll(listByAuthors);
            authorsListIsNull=false;
        }
        if(StringUtils.isNotBlank(bookEntity.getTitle())){
            List<BookEntity> listByTitles = bookRepository.findByTitleContaining(bookEntity.getTitle());
            addCommonRecords(listOfResults, listByTitles, authorsListIsNull);
            titlesListIsNull=false;
        }
        if(StringUtils.isNotBlank(com.mateusz.library.utils.StringUtils.nullSafeToString(bookEntity.getPrice()))) {
            List<BookEntity> listByPrices = bookRepository.findByPrice(bookEntity.getPrice());
            addCommonRecords(listOfResults, listByPrices, titlesListIsNull && authorsListIsNull);
            priceListIsNull=false;
        }
        if(bookEntity.getCategoriesList()!=null && !bookEntity.getCategoriesList().isEmpty()) {
            List<BookEntity> listByCategories = new ArrayList<>();
            boolean previousListIsNull = true;
            for (CategoryEntity category: bookEntity.getCategoriesList()){
                List<BookEntity> listOfBooksByCurrentCategory = bookRepository.findByCategoriesList_name(category.getName());
                addCommonRecords(listByCategories, listOfBooksByCurrentCategory, previousListIsNull);
                previousListIsNull=false;
            }
            addCommonRecords(listOfResults, listByCategories, priceListIsNull && authorsListIsNull && titlesListIsNull);
        }
        if (listOfResults.isEmpty()) {
            throw new NoResultException("no results");
        }
        return listOfResults.stream().distinct().collect(Collectors.toList());
    }

    private void addCommonRecords(List<BookEntity> listOfResults, List<BookEntity> currentList, boolean previousListIsNull) {
        if (listOfResults.isEmpty() && previousListIsNull){
            listOfResults.addAll(currentList);
        }
        listOfResults.retainAll(currentList);
    }

    private List<CategoryEntity> setExistingCategories(List<CategoryEntity> listOfCategories) {
        List<CategoryEntity> listToSet = new ArrayList<>();
        for (CategoryEntity category : listOfCategories){
            Optional<CategoryEntity> categoryFromDB = categoryRepository.findCategoryByName(category.getName().toLowerCase());
            if (categoryFromDB.isPresent()){
                listToSet.add(categoryFromDB.get());
            } else {
                category.setName(category.getName().toLowerCase());
                listToSet.add(category);
            }
        }
        return listToSet;
    }

    public BookEntity rentBook(String bookTitle) {
        UserEntity loggedUser = getLoggedUser();
        BookEntity bookToRent = bookRepository.findByTitleAndPresentIsTrue(bookTitle).stream()
                .findFirst()
                .orElseThrow(() -> new NoResultException("book is not available"));
        bookToRent.setPresent(false);
        bookToRent.setCurrentUser(loggedUser);
        bookToRent.setDateOfRent(DateUtils.parseDateToLocalDate(new Date()));
        loggedUser.getRentedBooks().add(bookToRent);
//        createHistoryOfBookEntity(loggedUser, bookToRent);
        return null;
    }

    public BookEntity returnBook(BookEntity bookEntity) {

    }
    @PreAuthorize("hasAuthority('user:delete')")
    public UserEntity getBookOwnerByBookId(Long bookId) {
       BookEntity book = bookRepository.findById(bookId).orElseThrow(() -> new NoResultException("book doesn't exist"));
       return book.getCurrentUser();
    }

    private HistoryOfBookEntity createHistoryOfBookEntity(UserEntity loggedUser, BookEntity bookToRent) {
    HistoryOfBookEntity historyOfBookEntity = new HistoryOfBookEntity();
    historyOfBookEntity.setBookEntity(bookToRent);
    historyOfBookEntity.setUserEntity(loggedUser);
    historyOfBooksRepository.save(historyOfBookEntity);
    return historyOfBookEntity;
    }


    private UserEntity getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getPrincipal().toString();
        return userRepository.findUserByUsername(username);
    }
}
