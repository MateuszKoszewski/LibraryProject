package com.mateusz.library.services;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.AddUserRequest;
import com.mateusz.library.model.dto.AddUserResponse;
import com.mateusz.library.model.dto.GetBookResponse;
import com.mateusz.library.model.dto.GetUserResponse;
import com.mateusz.library.repositories.UserRepository;
import com.mateusz.library.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private Logger LOGGER = LoggerFactory.getLogger(getClass());
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findUserByUsername(username);
        if (userEntity == null){
            LOGGER.error("User not found by login: " + username);
            throw new UsernameNotFoundException("User not found by login: " + username);
        } else {
            userEntity.setLastLoginDateDisplay(userEntity.getLastLoginDate());
            userEntity.setLastLoginDate(new Date());
            userRepository.save(userEntity);
            UserPrincipal userPrincipal = new UserPrincipal(userEntity);
            LOGGER.info("Returning found user by login: " + username);
            return userPrincipal;
        }
    }
}
