package com.mateusz.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mateusz.library.constants.NotificationMessages;
import com.mateusz.library.constants.Role;
import com.mateusz.library.constants.SecurityConstants;
import com.mateusz.library.constants.SuperAdminRoleConstants;
import com.mateusz.library.model.dao.*;
import com.mateusz.library.model.dto.HistoryOfBookForUserResponse;
import com.mateusz.library.security.JWTTokenProvider;
import com.mateusz.library.security.UserPrincipal;
import com.mateusz.library.utils.DateUtils;
import com.mateusz.library.utils.PasswordEncoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TestUtils {

    public static UserEntity getSimpleUser() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(2L);
        userEntity.setFirstName("John");
        userEntity.setLastName("Smith");
        userEntity.setUsername("john1");
        userEntity.setEmail("john@john.com");
        userEntity.setPassword(PasswordEncoder.encodePassword("John123"));
        userEntity.setNotLocked(true);
        userEntity.setActive(true);
        userEntity.setLastLoginDateDisplay(userEntity.getLastLoginDate());
        userEntity.setLastLoginDate(new Date());
        userEntity.setAuthorities(Role.ROLE_USER.getAuthorities());
//        createBookForUser(userEntity);
//        createNotificationsForUser(userEntity);
        userEntity.setNotifications(new ArrayList<>());
        userEntity.setRentedBooks(new ArrayList<>());
        return userEntity;
    }

    public static UserEntity getSimpleUserWithPasswordDecoded() {
        UserEntity userEntity = getSimpleUser();
        userEntity.setPassword("John123");
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

    public static UserEntity getAnotherUserWithSameId() {
        UserEntity userEntity = getSimpleUser();
        userEntity.setFirstName("John2");
        userEntity.setLastName("Smith2");
        userEntity.setUsername("john12");
        userEntity.setEmail("john@john.com2");
        userEntity.setPassword(PasswordEncoder.encodePassword("John1232"));
        return userEntity;
    }

    public static UserEntity getAnotherUser() {
        UserEntity userEntity = getSimpleUser();
        userEntity.setId(3L);
        userEntity.setFirstName("John3");
        userEntity.setLastName("Smith3");
        userEntity.setUsername("john13");
        userEntity.setEmail("john@john.com3");
        userEntity.setPassword("John1233");
        userEntity.setAuthorities(Role.ROLE_USER.getAuthorities());
        return userEntity;
    }

    public static HistoryOfBookEntity getHistoryOfBookEntity(UserEntity userEntity) {
        HistoryOfBookEntity historyOfBookEntity = new HistoryOfBookEntity();
        historyOfBookEntity.setId(1L);
        historyOfBookEntity.setUserEntity(userEntity);
        historyOfBookEntity.setBookEntity(createSimpleBook());
        return historyOfBookEntity;
    }

    public static BookEntity createSimpleBook() {
        BookEntity bookEntity = new BookEntity();
        bookEntity.setId(2L);
        bookEntity.setTitle("The returning of the king".toLowerCase());
        bookEntity.setAuthor("Tolkien".toLowerCase());
        bookEntity.setPresent(true);
        bookEntity.setPrice(BigDecimal.valueOf(10L));
        setCategoriesForBook(bookEntity, "przygodowa", "science-fiction");
        return bookEntity;
    }

    public static BookEntity createAnotherBook() {
        BookEntity bookEntity = new BookEntity();
        bookEntity.setId(3L);
        bookEntity.setTitle("The two towers");
        bookEntity.setAuthor("Tolkien");
        bookEntity.setPresent(true);
        return bookEntity;
    }

    public static List<HistoryOfBookForUserResponse> mapHistoryOfBooks(List<HistoryOfBookEntity> historyOfBooksByUsername) {
        return historyOfBooksByUsername.stream().map(historyOfBook -> new HistoryOfBookForUserResponse(historyOfBook.getBookEntity())).collect(Collectors.toList());
    }

    private static void setCategoriesForBook(BookEntity bookEntity, String... categories) {
        List<CategoryEntity> listOfCategories = new ArrayList<>();
        List<BookEntity> listOfBooks = new ArrayList<>();
        listOfBooks.add(bookEntity);
        for (String category: categories) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setName(category);
            categoryEntity.setListOfBooks(listOfBooks);
            listOfCategories.add(categoryEntity);
        }
        bookEntity.setCategoriesList(listOfCategories);
    }

    public static MvcResult getResultByRequestBody(MockMvc mockMvc, ObjectMapper objectMapper, JWTTokenProvider jwtTokenProvider, HttpMethod httpMethod, String url, UserEntity loggedInUser, Object returnedObject) throws Exception {
        return switch (httpMethod) {
            case DELETE -> mockMvc.perform(delete(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(returnedObject))
                            .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX +
                                    jwtTokenProvider.generateJwtToken(new UserPrincipal(loggedInUser))))
                    .andReturn();
            case POST -> mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(returnedObject))
                            .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX +
                                    jwtTokenProvider.generateJwtToken(new UserPrincipal(loggedInUser))))
                    .andReturn();
            case GET -> mockMvc.perform(MockMvcRequestBuilders.get(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(returnedObject))
                            .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX +
                                    jwtTokenProvider.generateJwtToken(new UserPrincipal(loggedInUser))))
                    .andReturn();
            default -> null;
        };
    }

    public static UserEntity getAdmin() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername(SuperAdminRoleConstants.ADMIN_USERNAME);
        userEntity.setEmail(SuperAdminRoleConstants.ADMIN_EMAIL);
        userEntity.setAuthorities(Role.ROLE_SUPER_ADMIN.getAuthorities());
        return userEntity;
    }
}
