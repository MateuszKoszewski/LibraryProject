package com.mateusz.library.controllers;

import com.mateusz.library.exception.ExceptionHandling;
import com.mateusz.library.model.dto.AddUserRequest;
import com.mateusz.library.model.dto.AddUserResponse;
import com.mateusz.library.model.dto.GetUserResponse;
import com.mateusz.library.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController extends ExceptionHandling {

    private final UserService userService;

    @GetMapping("/api/getAllUsers")
    public List<GetUserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/api/user")
    public AddUserResponse addUser(@RequestBody AddUserRequest addUserRequest){
        return userService.addUser(addUserRequest);
    }
    @GetMapping("/api/user/{email}")
    public GetUserResponse getParticularUser(@PathVariable(name="email") String email){
        return userService.getParticularUser(email);
    }

}
