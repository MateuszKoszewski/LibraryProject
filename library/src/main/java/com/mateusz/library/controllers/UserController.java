package com.mateusz.library.controllers;

import com.mateusz.library.constants.SecurityConstants;
import com.mateusz.library.exception.ExceptionHandling;
import com.mateusz.library.exception.domain.EmailExistsException;
import com.mateusz.library.exception.domain.UserNotFoundException;
import com.mateusz.library.exception.domain.UsernameExistsException;
import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.HistoryOfBookForUserResponse;
import com.mateusz.library.security.JWTTokenProvider;
import com.mateusz.library.security.UserPrincipal;
import com.mateusz.library.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
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

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody @Valid UserEntity userEntity) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        UserEntity newUser = userService.register(userEntity.getFirstName(), userEntity.getLastName(), userEntity.getUsername(), userEntity.getEmail(), userEntity.getPassword());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }
    @PostMapping("/changeUserData/{userId}")
    public ResponseEntity<UserEntity> changeUserData(@RequestBody @Valid UserEntity userEntity, @PathVariable(name = "userId") Long userId ) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        UserEntity editedUser = userService.changeUserData(userId, userEntity.getFirstName(), userEntity.getLastName(), userEntity.getUsername(), userEntity.getEmail(), userEntity.getPassword());
        return new ResponseEntity<>(editedUser, HttpStatus.OK);
    }
    @PostMapping("changeMyData")
    public ResponseEntity<UserEntity> changeMyData(@RequestBody @Valid UserEntity userEntity) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        UserEntity editedUser = userService.changeUserData(null, userEntity.getFirstName(), userEntity.getLastName(), userEntity.getUsername(), userEntity.getEmail(), userEntity.getPassword());
        return new ResponseEntity<>(editedUser, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<UserEntity> login(@RequestBody UserEntity userEntity) throws UserNotFoundException {
        authenticate(userEntity.getUsername(), userEntity.getPassword());
        UserEntity loginUser = userService.findUserByUsername(userEntity.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }
    @PreAuthorize("authentication.principal.equals(#username)")
    @DeleteMapping("/deleteByUsername/{username}")
    public ResponseEntity<UserEntity> deleteUserByUsername(@PathVariable(name = "username") String username) throws UserNotFoundException {
        UserEntity deletedUser = userService.deleteUser(username);
        return new ResponseEntity<>(deletedUser, HttpStatus.OK);
    }

    @DeleteMapping("/deleteById/{userId}")
    public ResponseEntity<UserEntity> deleteUserById(@PathVariable(name = "userId") Long userId){
        UserEntity deletedUser = userService.deleteUserById(userId);
        return new ResponseEntity<>(deletedUser, HttpStatus.OK);
    }

    @GetMapping("/getCurrentlyRentedBooks")
    public ResponseEntity<List<BookEntity>> getCurrentlyRentedBooks() {
        List<BookEntity> rentedBooks = userService.getCurrentlyRentedBooks();
        return new ResponseEntity<>(rentedBooks, HttpStatus.OK);
    }
    @GetMapping("/getMyHistory")
    public ResponseEntity<List<HistoryOfBookForUserResponse>> getMyHistory() {
        List<HistoryOfBookForUserResponse> myHistory = userService.getMyHistory();
        return new ResponseEntity<>(myHistory, HttpStatus.OK);
    }
    @GetMapping("/getUserHistoryById/{userId}")
    public ResponseEntity<List<HistoryOfBookForUserResponse>> getUserHistoryById(@PathVariable(name = "userId")Long userId) throws UserNotFoundException {
        List<HistoryOfBookForUserResponse> userHistory = userService.getUserHistoryById(userId);
        return new ResponseEntity<>(userHistory, HttpStatus.OK);
    }

    @GetMapping("/getUserHistoryByUsername/{username}")
    public ResponseEntity<List<HistoryOfBookForUserResponse>> getUserHistoryByUsername(@PathVariable(name = "username")String username) throws UserNotFoundException {
        List<HistoryOfBookForUserResponse> userHistory = userService.getUserHistoryByUsername(username);
        return new ResponseEntity<>(userHistory, HttpStatus.OK);
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
