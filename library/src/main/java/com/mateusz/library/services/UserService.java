package com.mateusz.library.services;

import com.mateusz.library.constants.Role;
import com.mateusz.library.model.dao.RoleEntity;
import com.mateusz.library.constants.UserServiceConstants;
import com.mateusz.library.exception.domain.EmailExistsException;
import com.mateusz.library.exception.domain.UserNotFoundException;
import com.mateusz.library.exception.domain.UsernameExistsException;
import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.UserEntity;
import com.mateusz.library.model.dto.AddUserRequest;
import com.mateusz.library.model.dto.AddUserResponse;
import com.mateusz.library.model.dto.GetBookResponse;
import com.mateusz.library.model.dto.GetUserResponse;
import com.mateusz.library.repositories.RolesRepository;
import com.mateusz.library.repositories.UserRepository;
import com.mateusz.library.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;

    private final RolesRepository rolesRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public List<GetUserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(user -> GetUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getFirstName())
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
        userEntity.setFirstName(addUserRequest.getName());
        userEntity.setLastName(addUserRequest.getLastName());
        userRepository.save(userEntity);
        return AddUserResponse.builder().email(addUserRequest.getEmail()).build();
    }

    public GetUserResponse getParticularUser(String email){
        UserEntity userEntity = userRepository.findUserByEmail(email);
        return GetUserResponse.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .name(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .listOfBooks(mapBookEntityToGetBookResponse(userEntity.getRentedBooks()))
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findUserByUsername(username);
        if (userEntity == null){
            LOGGER.error(UserServiceConstants.USER_NOT_FOUND_BY_USERNAME + username);
            throw new UsernameNotFoundException(UserServiceConstants.USER_NOT_FOUND_BY_USERNAME + username);
        } else {
            userEntity.setLastLoginDateDisplay(userEntity.getLastLoginDate());
            userEntity.setLastLoginDate(new Date());
            userRepository.save(userEntity);
            UserPrincipal userPrincipal = new UserPrincipal(userEntity);
            LOGGER.info("Returning found user by login: " + username);
            return userPrincipal;
        }
    }

    public UserEntity register(String firstName, String lastName, String username, String email, String password) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
        UserEntity userEntity = new UserEntity();
        String encodedPassword = encodePassword(password);
        userEntity.setFirstName(firstName);
        userEntity.setLastName(lastName);
        userEntity.setEmail(email);
        userEntity.setUsername(username);
        userEntity.setJoinDate(new Date());
        userEntity.setPassword(encodedPassword);
        userEntity.setActive(true);
        userEntity.setNotLocked(true);
        userEntity.setRoles(findDefaultRolesForUser());
        userEntity.setAuthorities(Role.ROLE_USER.getAuthorities());
        userRepository.save(userEntity);
        return null;
    }

    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    public UserEntity findUserByUsername (String username){
        return userRepository.findUserByUsername(username);
    }

    public UserEntity findUserByEmail (String email) {
        return userRepository.findUserByEmail(email);
    }

    private UserEntity validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        UserEntity userByUsername = findUserByUsername(newUsername);
        UserEntity userByEmail = findUserByUsername(newEmail);
        if (StringUtils.isNotBlank(currentUsername)) {
            UserEntity currentUser = findUserByUsername(currentUsername);
            if (currentUser == null) {
                throw new UserNotFoundException("No user found by username " + currentUsername);
            }
            if (userByUsername != null && !currentUser.getId().equals(userByUsername.getId())) {
                throw new UsernameExistsException(UserServiceConstants.USERNAME_ALREADY_EXISTS);
            }
            if (userByEmail != null && !currentUser.getId().equals(userByEmail.getId())) {
                throw new EmailExistsException(UserServiceConstants.EMAIL_ALREADY_EXISTS);
            }
            return currentUser;
        } else {
            if (userByUsername != null) {
                throw new UsernameExistsException(UserServiceConstants.USERNAME_ALREADY_EXISTS);
            }
            if (userByEmail != null){
                throw new EmailExistsException(UserServiceConstants.EMAIL_ALREADY_EXISTS);
            }
            return null;
        }
    }

    private List<RoleEntity> findDefaultRolesForUser(){
        List<RoleEntity> listOfRoles = new ArrayList<>();
        listOfRoles.add(rolesRepository.findRoleEntityByRoleEnum(Role.ROLE_USER));
        return listOfRoles;
    }
}
