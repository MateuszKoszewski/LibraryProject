package com.mateusz.library.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mateusz.library.TestUtils;
import com.mateusz.library.configuration.TestConfig;
import com.mateusz.library.constants.SecurityConstants;
import com.mateusz.library.exception.domain.EmailExistsException;
import com.mateusz.library.exception.domain.UserNotFoundException;
import com.mateusz.library.exception.domain.UsernameExistsException;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.security.JWTTokenProvider;
import com.mateusz.library.security.JwtAccessDeniedHandler;
import com.mateusz.library.security.UserPrincipal;
import com.mateusz.library.security.configuration.SecurityConfiguration;
import com.mateusz.library.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

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
        //
        //Then
        //
        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEntity)))
                .andExpect(MockMvcResultMatchers.status().isOk());
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
        //
        //Then
        //
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEntityWithPasswordDecoded)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void shouldChangeUserDataById() throws Exception {
        //
        //Given
        //
        UserEntity userEntityWhomDataAreChanged = TestUtils.getSimpleUser();
        UserEntity newUserData = TestUtils.getAnotherUserWithSameId();
//        JWTTokenProvider jwtTokenProvider = new JWTTokenProvider();
        //
        //When
        //
        Mockito.when(userService.changeUserData(userEntityWhomDataAreChanged.getId(), newUserData.getFirstName(), newUserData.getLastName(), newUserData.getUsername(), newUserData.getEmail(), newUserData.getPassword())).thenReturn(newUserData);
        //
        //Then
        //
        mockMvc.perform(post("/api/user/changeUserData/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserData))
                        .header(HttpHeaders.AUTHORIZATION, SecurityConstants.TOKEN_PREFIX +
                                jwtTokenProvider.generateJwtToken(new UserPrincipal(userEntityWhomDataAreChanged))))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }



}
