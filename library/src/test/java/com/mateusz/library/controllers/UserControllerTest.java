package com.mateusz.library.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mateusz.library.TestUtils;
import com.mateusz.library.configuration.TestConfig;
import com.mateusz.library.constants.SecurityConstants;
import com.mateusz.library.exception.domain.EmailExistsException;
import com.mateusz.library.exception.domain.UserNotFoundException;
import com.mateusz.library.exception.domain.UsernameExistsException;
import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.HistoryOfBookEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.HistoryOfBookForUserResponse;
import com.mateusz.library.security.JWTTokenProvider;
import com.mateusz.library.security.JwtAccessDeniedHandler;
import com.mateusz.library.security.UserPrincipal;
import com.mateusz.library.security.configuration.SecurityConfiguration;
import com.mateusz.library.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @MockBean
    private UserService userService;

    @Test
    void shouldRegisterUser() throws Exception {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        //
        //When
        //
        Mockito.when(userService.register(userEntity.getFirstName(), userEntity.getLastName(), userEntity.getUsername(), userEntity.getEmail(), userEntity.getPassword()))
                .thenReturn(userEntity);
        MvcResult result = mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEntity)))
                        .andReturn();
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(userEntity), result.getResponse().getContentAsString());
    }

    @Test
    void shouldLogInUser() throws Exception {
        //
        //Given
        //
        UserEntity userEntityWithPasswordDecoded = TestUtils.getSimpleUserWithPasswordDecoded();
        UserEntity userEntity = TestUtils.getSimpleUser();
        //
        //When
        //
        Mockito.when(userService.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        Mockito.when(userService.loadUserByUsername(userEntity.getUsername())).thenReturn(new UserPrincipal(userEntity));
        MvcResult result = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEntityWithPasswordDecoded)))
                .andReturn();
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(userEntity), result.getResponse().getContentAsString());
    }

    @Test
    void shouldChangeUserDataById() throws Exception {
        //
        //Given
        //
        UserEntity userEntityWhomDataAreChanged = TestUtils.getSimpleUser();
        UserEntity newUserData = TestUtils.getAnotherUserWithSameId();
        //
        //When
        //
        Mockito.when(userService.changeUserData(userEntityWhomDataAreChanged.getId(), newUserData.getFirstName(), newUserData.getLastName(), newUserData.getUsername(), newUserData.getEmail(), newUserData.getPassword())).thenReturn(newUserData);
        MvcResult result = getResultByRequestBody(HttpMethod.POST, "/api/user/changeUserData/" + userEntityWhomDataAreChanged.getId(), userEntityWhomDataAreChanged, newUserData);
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(newUserData), result.getResponse().getContentAsString());
    }
@Test
    void shouldChangeMyData() throws Exception {
        //
        //Given
        //
        UserEntity userEntityWhomDataAreChanged = TestUtils.getSimpleUser();
        UserEntity newUserData = TestUtils.getAnotherUserWithSameId();
        //
        //When
        //
        Mockito.when(userService.changeUserData(null, newUserData.getFirstName(), newUserData.getLastName(), newUserData.getUsername(), newUserData.getEmail(), newUserData.getPassword())).thenReturn(newUserData);
        MvcResult result = getResultByRequestBody(HttpMethod.POST, "/api/user/changeMyData", userEntityWhomDataAreChanged, newUserData);
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(newUserData), result.getResponse().getContentAsString());
    }
    @Test
    void shouldDeleteUserByUsername() throws Exception {
        //
        //Given
        //
        UserEntity userToDelete = TestUtils.getSimpleUser();
        //
        //When
        //
        Mockito.when(userService.deleteUser(userToDelete.getUsername())).thenReturn(userToDelete);
        MvcResult result = getResultByRequestBody(HttpMethod.DELETE, "/api/user/deleteByUsername/" + userToDelete.getUsername(), userToDelete, userToDelete);
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(userToDelete), result.getResponse().getContentAsString());
    }
    @Test
    void shouldNotDeleteUserByUsername_userTryingToDeleteAnotherUser() throws Exception {
        //
        //Given
        //
        UserEntity userToDelete = TestUtils.getSimpleUser();
        UserEntity anotherUser = TestUtils.getAnotherUser();
        //
        //When
        //
        Mockito.when(userService.deleteUser(userToDelete.getUsername())).thenReturn(userToDelete);
        MvcResult result = getResultByRequestBody(HttpMethod.DELETE, "/api/user/deleteByUsername/" + userToDelete.getUsername(), anotherUser, userToDelete);
        //
        //Then
        //
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }
@Test
    void shouldDeleteUserById() throws Exception {
        //
        //Given
        //
        UserEntity userToDelete = TestUtils.getSimpleUser();
        //
        //When
        //
        Mockito.when(userService.deleteUserById(userToDelete.getId())).thenReturn(userToDelete);
        MvcResult result = getResultByRequestBody(HttpMethod.DELETE, "/api/user/deleteById/" + userToDelete.getId(), userToDelete, userToDelete);
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(userToDelete), result.getResponse().getContentAsString());
    }
    @Test
    void shouldGetCurrentlyRentedBooks() throws Exception {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        BookEntity bookEntity = TestUtils.createBookForUser(userEntity);
        //
        //When
        //
        Mockito.when(userService.getCurrentlyRentedBooks()).thenReturn(List.of(bookEntity));
        MvcResult result = getResultByRequestBody(HttpMethod.GET, "/api/user/getCurrentlyRentedBooks", userEntity, List.of(bookEntity));
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(List.of(bookEntity)), result.getResponse().getContentAsString());
    }
    @Test
    void shouldGetMyHistory() throws Exception {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        List<HistoryOfBookEntity> listOfHistory = List.of(TestUtils.getHistoryOfBookEntity(userEntity));
        List<HistoryOfBookForUserResponse> mappedListOfHistory = TestUtils.mapHistoryOfBooks(listOfHistory);
        //
        //When
        //
        Mockito.when(userService.getMyHistory()).thenReturn(mappedListOfHistory);
        MvcResult result = getResultByRequestBody(HttpMethod.GET, "/api/user/getMyHistory", userEntity, mappedListOfHistory);
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(mappedListOfHistory), result.getResponse().getContentAsString());
    }
    @Test
    void shouldGetUserHistoryById() throws Exception {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        List<HistoryOfBookEntity> listOfHistory = List.of(TestUtils.getHistoryOfBookEntity(userEntity));
        List<HistoryOfBookForUserResponse> mappedListOfHistory = TestUtils.mapHistoryOfBooks(listOfHistory);
        //
        //When
        //
        Mockito.when(userService.getUserHistoryById(userEntity.getId())).thenReturn(mappedListOfHistory);
        MvcResult result = getResultByRequestBody(HttpMethod.GET, "/api/user/getUserHistoryById/" + userEntity.getId(), userEntity, mappedListOfHistory);
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(mappedListOfHistory), result.getResponse().getContentAsString());
    }
@Test
    void shouldGetUserHistoryByUsername() throws Exception {
        //
        //Given
        //
        UserEntity userEntity = TestUtils.getSimpleUser();
        List<HistoryOfBookEntity> listOfHistory = List.of(TestUtils.getHistoryOfBookEntity(userEntity));
        List<HistoryOfBookForUserResponse> mappedListOfHistory = TestUtils.mapHistoryOfBooks(listOfHistory);
        //
        //When
        //
        Mockito.when(userService.getUserHistoryByUsername(userEntity.getUsername())).thenReturn(mappedListOfHistory);
        MvcResult result = getResultByRequestBody(HttpMethod.GET, "/api/user/getUserHistoryByUsername/" + userEntity.getUsername(), userEntity, mappedListOfHistory);
        //
        //Then
        //
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(objectMapper.writeValueAsString(mappedListOfHistory), result.getResponse().getContentAsString());
    }


private MvcResult getResultByRequestBody(HttpMethod httpMethod, String url, UserEntity loggedInUser, Object returnedObject) throws Exception {
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

}
