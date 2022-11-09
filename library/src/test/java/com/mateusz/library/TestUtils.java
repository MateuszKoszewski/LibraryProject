package com.mateusz.library;

import com.mateusz.library.constants.NotificationMessages;
import com.mateusz.library.constants.Role;
import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;
import com.mateusz.library.model.dao.NotificationEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.security.UserPrincipal;
import com.mateusz.library.utils.DateUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestUtils {

    public static UserEntity getSimpleUser() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(2L);
        userEntity.setFirstName("John");
        userEntity.setLastName("Smith");
        userEntity.setUsername("john1");
        userEntity.setEmail("john@john.com");
        userEntity.setPassword("John123");
        userEntity.setAuthorities(Role.ROLE_USER.getAuthorities());
//        createBookForUser(userEntity);
//        createNotificationsForUser(userEntity);
        return userEntity;
    }

    public static BookEntity createBookForUser(UserEntity userEntity) {
        BookEntity bookEntity = new BookEntity();
        bookEntity.setId(1L);
        bookEntity.setCurrentUser(userEntity);
        bookEntity.setTitle("Hobbit");
        bookEntity.setAuthor("Tolkien");
        bookEntity.setPresent(false);
//        bookEntity.setDateOfRent(DateUtils.createLocalDate("2022-11-08"));
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName("science-fiction");
        categoryEntity.setId(1L);
        List<CategoryEntity> listOfCategories = new ArrayList<>();
        listOfCategories.add(categoryEntity);
        bookEntity.setCategoriesList(listOfCategories);
        List<BookEntity> listOfBooks = new ArrayList<>();
        listOfBooks.add(bookEntity);
        categoryEntity.setListOfBooks(listOfBooks);
        userEntity.setRentedBooks(listOfBooks);
        return bookEntity;
    }

    public static void authenticateUser (UserEntity userEntity){
        UserPrincipal userPrincipal = new UserPrincipal(userEntity);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userPrincipal.getUsername(), null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }
}
