package com.mateusz.library.services;

import com.mateusz.library.constants.Role;
import com.mateusz.library.model.dao.*;
import com.mateusz.library.constants.UserServiceConstants;
import com.mateusz.library.exception.domain.EmailExistsException;
import com.mateusz.library.exception.domain.UserNotFoundException;
import com.mateusz.library.exception.domain.UsernameExistsException;
import com.mateusz.library.model.dto.*;
import com.mateusz.library.repositories.HistoryOfBooksRepository;
import com.mateusz.library.repositories.NotificationRepository;
import com.mateusz.library.repositories.RolesRepository;
import com.mateusz.library.repositories.UserRepository;
import com.mateusz.library.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

    private final NotificationRepository notificationRepository;
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
            LOGGER.info("Returning found user by username: " + username);
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
@PreAuthorize("authentication.principal.equals(#username)")
    public UserEntity deleteUser(String username){
        UserEntity userToDelete = userRepository.findUserByUsername(username);
        userRepository.delete(userToDelete);
        return null;
    }

    public UserEntity deleteUserById(Long userId) {
        UserEntity userToDelete = userRepository.findById(userId).orElseThrow(()-> new NoResultException("specified User doesn't exist in DB"));
        if (!operationIsAllowed(userToDelete)) {

            LOGGER.error("unauthorized user: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal() + " tried to delete user: " + userToDelete.getUsername());
            throw new AccessDeniedException("access denied");
        }
        userRepository.delete(userToDelete);
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

    public List<HistoryOfBookForUserResponse> getUserHistoryById(Long userId) {
        return mapHistoryOfBooks(historyOfBooksRepository.findByUserEntity_id(userId));
    }

    public List<HistoryOfBookForUserResponse> getUserHistoryByUsername(String username) {
        return mapHistoryOfBooks(historyOfBooksRepository.findByUserEntity_username(username));
    }

    public List<NotificationEntity> getLoggedInUserAllNotifications() {
        List<NotificationEntity> listOfUserNotifications = getLoggedInUserNotifications();
        List<NotificationEntity> duplicatedListOfUserNotifications = duplicateListOfNotifications(listOfUserNotifications);
        setNotificationsAlreadyReadToTrue(listOfUserNotifications);
        return duplicatedListOfUserNotifications;
    }

    private List<NotificationEntity> getLoggedInUserNotifications() {
        UserEntity loggedInUser = getLoggedInUserEntity();
        return loggedInUser.getNotifications();
    }

    private List<NotificationEntity> duplicateListOfNotifications(List<NotificationEntity> listOfUserNotifications) {
        List<NotificationEntity> duplicatedList = new ArrayList<>();
         listOfUserNotifications.forEach(notification -> {
            NotificationEntity duplicatedNotification = NotificationEntity.builder()
                    .bookEntity(notification.getBookEntity())
                    .userEntity(notification.getUserEntity())
                    .creationTime(notification.getCreationTime())
                    .alreadyRead(notification.isAlreadyRead())
                    .readingTimeByUser(notification.getReadingTimeByUser())
                    .message(notification.getMessage())
                    .id(notification.getId())
                    .build();
            duplicatedList.add(duplicatedNotification);
        });
         return duplicatedList;
    }

    public List<NotificationEntity> getLoggedInUserCurrentNotifications() {
        List<NotificationEntity> allNotifications = getLoggedInUserAllNotifications();
        return allNotifications.stream().filter(notification -> !notification.isAlreadyRead()).toList();
    }

    public List<NotificationEntity> deleteNotifications() {
        UserEntity loggedInUser = getLoggedInUserEntity();
        List<NotificationEntity> listOfUserNotifications = loggedInUser.getNotifications();
        List<NotificationEntity> listWithNotificationsToRemove = new ArrayList<>();

        for (NotificationEntity notification : listOfUserNotifications) {
            if (notification.isAlreadyRead()) {
                listWithNotificationsToRemove.add(notification);
            }
        }
        loggedInUser.removeParticularNotifications(listWithNotificationsToRemove);
        return listWithNotificationsToRemove;
    }

    private void setNotificationsAlreadyReadToTrue(List<NotificationEntity> listOfUserNotifications) {
        for(NotificationEntity notification: listOfUserNotifications){
            notification.setAlreadyRead(true);
        }
    }

    private List<HistoryOfBookForUserResponse> mapHistoryOfBooks(List<HistoryOfBookEntity> historyOfBooksByUsername) {
        return historyOfBooksByUsername.stream().map(historyOfBook -> new HistoryOfBookForUserResponse(historyOfBook.getBookEntity())).collect(Collectors.toList());
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

    private boolean operationIsAllowed(UserEntity userEntity) {
        Authentication authentication = getAuthentication();
            return authentication.getPrincipal().equals(userEntity.getUsername()) || authentication.getPrincipal().equals("admin");
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String getCurrentlyLoggedInUsername() {
        Authentication authentication = getAuthentication();
        return authentication.getPrincipal().toString();
    }

    private UserEntity getLoggedInUserEntity() {
        String currentlyLoggedInUsername = getCurrentlyLoggedInUsername();
        return userRepository.findUserByUsername(currentlyLoggedInUsername);
    }
}
