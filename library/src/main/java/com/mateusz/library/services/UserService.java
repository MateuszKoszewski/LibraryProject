package com.mateusz.library.services;

import com.mateusz.library.constants.ErrorMessages;
import com.mateusz.library.constants.Role;
import com.mateusz.library.constants.SuperAdminRoleConstants;
import com.mateusz.library.exception.ExceptionHandling;
import com.mateusz.library.model.dao.*;
import com.mateusz.library.constants.UserServiceConstants;
import com.mateusz.library.exception.domain.EmailExistsException;
import com.mateusz.library.exception.domain.UserNotFoundException;
import com.mateusz.library.exception.domain.UsernameExistsException;
import com.mateusz.library.model.dto.*;
import com.mateusz.library.repositories.HistoryOfBooksRepository;
import com.mateusz.library.repositories.RolesRepository;
import com.mateusz.library.repositories.UserRepository;
import com.mateusz.library.security.UserPrincipal;
import com.mateusz.library.utils.LibraryStringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
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

    private final HistoryOfBooksRepository historyOfBooksRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

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
            LOGGER.info("Returning found user by username: " + username);
            return userPrincipal;
        }
    }

    public UserEntity register(String firstName, String lastName, String username, String email, String password) throws UsernameExistsException, EmailExistsException {
        validateNewUsernameAndEmail(null, username, email);
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
        return userEntity;
    }

    public UserEntity changeUserData(Long userId, String firstName, String lastName, String username, String email, String password) throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        UserEntity userWhomDataAreChanging;
        if (userId!=null) {
            userWhomDataAreChanging = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("No user found by id"));
            if (operationIsAllowed(userWhomDataAreChanging, ErrorMessages.CHANGE_DATA_OF_USER)) {
                validateAndSetUserData(userWhomDataAreChanging, firstName, lastName, username, email, password);
            }
        } else {
            userWhomDataAreChanging = getLoggedInUserEntity();
            validateAndSetUserData(userWhomDataAreChanging, firstName, lastName, username, email, password);
        }
        return userWhomDataAreChanging;
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

    public UserEntity deleteUser(String username){
        UserEntity userToDelete = userRepository.findUserByUsername(username);
        userRepository.delete(userToDelete);
        return null;
    }

    public UserEntity deleteUserById(Long userId) {
        UserEntity userToDelete = userRepository.findById(userId).orElseThrow(()-> new NoResultException("specified User doesn't exist in DB"));
        if (operationIsAllowed(userToDelete, ErrorMessages.DELETE_USER)) {
            userRepository.delete(userToDelete);
        }
        return null;
    }

    public List<BookEntity> getCurrentlyRentedBooks() {
        String currentlyLoggedInUsername = getCurrentlyLoggedInUsername();
        UserEntity currentUser = userRepository.findUserByUsername(currentlyLoggedInUsername);
        return currentUser.getRentedBooks();
    }

    public List<HistoryOfBookForUserResponse> getMyHistory() {
        String currentlyLoggedUsername = getCurrentlyLoggedInUsername();
        return mapHistoryOfBooks(historyOfBooksRepository.findByUserEntity_username(currentlyLoggedUsername));
    }

    public List<HistoryOfBookForUserResponse> getUserHistoryById(Long userId) throws UserNotFoundException {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(ErrorMessages.NO_USER_FOUND_BY_ID));
        if (operationIsAllowed(userEntity, ErrorMessages.GET_HISTORY_OF_ANOTHER_USER)){
            return mapHistoryOfBooks(historyOfBooksRepository.findByUserEntity_id(userId));
        }
        return null;
    }

    public List<HistoryOfBookForUserResponse> getUserHistoryByUsername(String username) throws UserNotFoundException {
        UserEntity userEntity = userRepository.findUserByUsername(username);
        if (userEntity==null){
            throw new UserNotFoundException(ErrorMessages.NO_USER_FOUND_BY_USERNAME);
        }
        if (operationIsAllowed(userEntity, ErrorMessages.GET_HISTORY_OF_ANOTHER_USER)){
            return mapHistoryOfBooks(historyOfBooksRepository.findByUserEntity_username(username));
        }
        return null;
    }

    private List<HistoryOfBookForUserResponse> mapHistoryOfBooks(List<HistoryOfBookEntity> historyOfBooksByUsername) {
        return historyOfBooksByUsername.stream().map(historyOfBook -> new HistoryOfBookForUserResponse(historyOfBook.getBookEntity())).collect(Collectors.toList());
    }

    private UserEntity validateNewUsernameAndEmail(UserEntity userEntity, String newUsername, String newEmail) throws UsernameExistsException, EmailExistsException {
        UserEntity userByUsername = findUserByUsername(newUsername);
        UserEntity userByEmail = findUserByEmail(newEmail);
        if (userEntity!=null) {
            UserEntity currentUser = userEntity;
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

    private boolean operationIsAllowed(UserEntity userEntity, String operation) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException(ExceptionHandling.NOT_ENOUGH_PERMISSION);
        }
        boolean userOrAdminIsLoggedIn = authentication.getPrincipal().equals(userEntity.getUsername()) || authentication.getPrincipal().equals(SuperAdminRoleConstants.ADMIN_USERNAME);
        if (!userOrAdminIsLoggedIn) {
            LOGGER.error(ErrorMessages.UNAUTHORIZED_USER_TRIED_TO + operation + userEntity.getUsername());
            throw new AccessDeniedException(ExceptionHandling.NOT_ENOUGH_PERMISSION);
        }
        return true;
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String getCurrentlyLoggedInUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException(ExceptionHandling.NOT_ENOUGH_PERMISSION);
        }
        return authentication.getPrincipal().toString();
    }

    private UserEntity getLoggedInUserEntity() {
        String currentlyLoggedInUsername = getCurrentlyLoggedInUsername();
        return userRepository.findUserByUsername(currentlyLoggedInUsername);
    }

    private void setUserData(UserEntity userEntity, String firstName, String lastName, String username, String email, String password) {
        if(LibraryStringUtils.isNotBlankAndNull(firstName)) {
            userEntity.setFirstName(firstName);
        }
        if (LibraryStringUtils.isNotBlankAndNull(lastName)) {
            userEntity.setLastName(lastName);
        }
        if (LibraryStringUtils.isNotBlankAndNull(username)) {
            userEntity.setUsername(username);
        }
        if (LibraryStringUtils.isNotBlankAndNull(email)) {
            userEntity.setEmail(email);
        }
        if (LibraryStringUtils.isNotBlankAndNull(password)) {
            userEntity.setPassword(bCryptPasswordEncoder.encode(password));
        }
    }

    private void validateAndSetUserData(UserEntity userEntity, String firstName, String lastName, String username, String email, String password) throws UsernameExistsException, EmailExistsException {
        validateNewUsernameAndEmail(userEntity, username, email);
        setUserData(userEntity, firstName, lastName, username, email, password);
    }

}
