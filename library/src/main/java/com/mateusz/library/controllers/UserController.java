package com.mateusz.library.controllers;

import com.mateusz.library.constants.SecurityConstants;
import com.mateusz.library.exception.ExceptionHandling;
import com.mateusz.library.exception.domain.EmailExistsException;
import com.mateusz.library.exception.domain.UserNotFoundException;
import com.mateusz.library.exception.domain.UsernameExistsException;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.AddUserRequest;
import com.mateusz.library.model.dto.AddUserResponse;
import com.mateusz.library.model.dto.GetUserResponse;
import com.mateusz.library.security.JWTTokenProvider;
import com.mateusz.library.security.UserPrincipal;
import com.mateusz.library.services.UserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/user")
public class UserController extends ExceptionHandling {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private JWTTokenProvider jwtTokenProvider;

    @GetMapping("/getAllUsers")
    public List<GetUserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/user")
    public AddUserResponse addUser(@RequestBody AddUserRequest addUserRequest){
        return userService.addUser(addUserRequest);
    }
    @GetMapping("/api/user/{email}")
    public GetUserResponse getParticularUser(@PathVariable(name="email") String email){
        return userService.getParticularUser(email);
    }
    @GetMapping("/api/home")
    public GetUserResponse goToHomePage() {
        throw new BadCredentialsException("it's working");
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody @Valid UserEntity userEntity) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        UserEntity newUser = userService.register(userEntity.getFirstName(), userEntity.getLastName(), userEntity.getUsername(), userEntity.getEmail(), userEntity.getPassword());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<UserEntity> login(@RequestBody UserEntity userEntity) {
        authenticate(userEntity.getUsername(), userEntity.getPassword());
        UserEntity loginUser = userService.findUserByUsername(userEntity.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }

    @DeleteMapping("/deleteByUsername/{username}")
    public ResponseEntity<UserEntity> deleteUserByUsername(@PathVariable(name = "username") String username){
        UserEntity deletedUser = userService.deleteUser(username);
        return new ResponseEntity<>(deletedUser, HttpStatus.OK);
    }

    @DeleteMapping("/deleteById/{userId}")
    public ResponseEntity<UserEntity> deleteUserById(@PathVariable(name = "userId") Long userId){
        UserEntity deletedUser = userService.deleteUserById(userId);
        return new ResponseEntity<>(deletedUser, HttpStatus.OK);
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders header = new HttpHeaders();
        header.add(SecurityConstants.JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
        return header;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

}
