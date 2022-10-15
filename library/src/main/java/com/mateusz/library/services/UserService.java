package com.mateusz.library.services;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.AddUserRequest;
import com.mateusz.library.model.dto.AddUserResponse;
import com.mateusz.library.model.dto.GetBookResponse;
import com.mateusz.library.model.dto.GetUserResponse;
import com.mateusz.library.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public List<GetUserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(user -> GetUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .listOfBooks(mapBookEntityToGetBookResponse(user.getRentedBooks()))
                .build())
                .collect(Collectors.toList());
    }

    private List<GetBookResponse> mapBookEntityToGetBookResponse (List<BookEntity> listOfBookEntity){
        return listOfBookEntity.stream().map(bookEntity -> GetBookResponse.builder()
                .id(bookEntity.getId())
                .title(bookEntity.getTitle())
                .author(bookEntity.getAuthor())
                .build())
                .collect(Collectors.toList());
    }

    public AddUserResponse addUser(AddUserRequest addUserRequest){
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(addUserRequest.getEmail());
        userEntity.setName(addUserRequest.getName());
        userEntity.setLastName(addUserRequest.getLastName());
        userRepository.save(userEntity);
        return AddUserResponse.builder().email(addUserRequest.getEmail()).build();
    }

    public GetUserResponse getParticularUser(String email){
        UserEntity userEntity = userRepository.findByEmail(email);
        return GetUserResponse.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .name(userEntity.getName())
                .lastName(userEntity.getLastName())
                .listOfBooks(mapBookEntityToGetBookResponse(userEntity.getRentedBooks()))
                .build();
    }
}
