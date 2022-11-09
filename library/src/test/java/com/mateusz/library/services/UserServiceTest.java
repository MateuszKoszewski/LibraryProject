package com.mateusz.library.services;



import com.mateusz.library.constants.NotificationMessages;
import com.mateusz.library.constants.Role;
import com.mateusz.library.constants.SuperAdminRoleConstants;
import com.mateusz.library.exception.domain.EmailExistsException;
import com.mateusz.library.exception.domain.UserNotFoundException;
import com.mateusz.library.exception.domain.UsernameExistsException;
import com.mateusz.library.model.dao.*;
import com.mateusz.library.model.dto.HistoryOfBookForUserResponse;
import com.mateusz.library.repositories.HistoryOfBooksRepository;
import com.mateusz.library.repositories.RolesRepository;
import com.mateusz.library.repositories.UserRepository;
import com.mateusz.library.security.UserPrincipal;
import com.mateusz.library.utils.DateUtils;
import org.apache.catalina.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private HistoryOfBooksRepository historyOfBooksRepository;

    @Mock
    private BCryptPasswordEncoder userBCryptPasswordEncoder;

    @Test
    void shouldNotAddUserIfExist() {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        //
        //Then
        //
        assertThrows(UsernameExistsException.class, () -> userService.register(userEntity.getFirstName(), userEntity.getLastName(), userEntity.getUsername(), userEntity.getEmail(), userEntity.getPassword()));
    }

    @Test
    void shouldNotAddUserIfEmailExist() {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        userEntity.setUsername(null);
        //
        //When
        //
        Mockito.when(userRepository.findUserByEmail(userEntity.getEmail())).thenReturn(userEntity);
        //
        //Then
        //
        assertThrows(EmailExistsException.class, () -> userService.register(userEntity.getFirstName(), userEntity.getLastName(), userEntity.getUsername(), userEntity.getEmail(), userEntity.getPassword()));
    }

    @Test
    void shouldRegisterUser() throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findUserByEmail(userEntity.getEmail())).thenReturn(null);
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(null);
        Mockito.when(userBCryptPasswordEncoder.encode(userEntity.getPassword())).thenReturn(userEntity.getPassword());
        UserEntity registeredUser = userService.register(userEntity.getFirstName(), userEntity.getLastName(), userEntity.getUsername(), userEntity.getEmail(), userEntity.getPassword());
        //
        //Then
        //
        checkAllUserPersonalData(userEntity, registeredUser);
        assertNull(registeredUser.getRentedBooks());
        assertNull(registeredUser.getNotifications());
        assertNull(registeredUser.getAccountBalance());
        assertTrue(registeredUser.isActive());
        assertTrue(registeredUser.isNotLocked());
        assertEquals(new Date().toString(), registeredUser.getJoinDate().toString());
        assertNull(registeredUser.getLastLoginDate());
        assertNull(registeredUser.getLastLoginDateDisplay());
    }

    @Test
    void shouldChangeUserDataWithId() throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        //
        //Given
        //
        UserEntity userTryingChangeData = getSimpleUser();
        UserEntity userWithAlreadyChangedData = getAnotherUserWithSameId();
        //
        //When
        //
        Mockito.when(userRepository.findById(userTryingChangeData.getId())).thenReturn(Optional.of(userTryingChangeData));
        Mockito.when(userRepository.findUserByUsername(userWithAlreadyChangedData.getUsername())).thenReturn(null);
        Mockito.when(userRepository.findUserByEmail(userWithAlreadyChangedData.getEmail())).thenReturn(null);
        authenticateUser(userTryingChangeData);
        Mockito.when(userBCryptPasswordEncoder.encode(userWithAlreadyChangedData.getPassword())).thenReturn(userWithAlreadyChangedData.getPassword());
        UserEntity userWithChangedData = userService.changeUserData(userTryingChangeData.getId(),
                userWithAlreadyChangedData.getFirstName(), userWithAlreadyChangedData.getLastName(),
                userWithAlreadyChangedData.getUsername(), userWithAlreadyChangedData.getEmail(),
                userWithAlreadyChangedData.getPassword());
        //
        //Then
        //
        checkAllUserPersonalData(userWithAlreadyChangedData, userWithChangedData);

    }

    @Test
    void shouldChangeUserDataWithoutId() throws UserNotFoundException, UsernameExistsException, EmailExistsException {
        //
        //Given
        //
        UserEntity userTryingChangeData = getSimpleUser();
        UserEntity userWithAlreadyChangedData = getAnotherUserWithSameId();
        //
        //When
        //
        Mockito.when(userRepository.findUserByUsername(userTryingChangeData.getUsername())).thenReturn(userTryingChangeData);
        Mockito.when(userRepository.findUserByUsername(userWithAlreadyChangedData.getUsername())).thenReturn(null);
        Mockito.when(userRepository.findUserByEmail(userWithAlreadyChangedData.getEmail())).thenReturn(null);
        authenticateUser(userTryingChangeData);
        Mockito.when(userBCryptPasswordEncoder.encode(userWithAlreadyChangedData.getPassword())).thenReturn(userWithAlreadyChangedData.getPassword());
        UserEntity userWithChangedData = userService.changeUserData(null, userWithAlreadyChangedData.getFirstName(),
                userWithAlreadyChangedData.getLastName(), userWithAlreadyChangedData.getUsername(),
                userWithAlreadyChangedData.getEmail(), userWithAlreadyChangedData.getPassword());
        //
        //Then
        //
        checkAllUserPersonalData(userWithAlreadyChangedData, userWithChangedData);
//        clearContext();
    }

    @Test
    void shouldNotChangeUserData_wrongId() {
        //
        //Given
        //
        UserEntity userTryingChangeData = getSimpleUser();
        UserEntity userWithAlreadyChangedData = getAnotherUserWithSameId();
        //
        //When
        //
        Mockito.when(userRepository.findById(userTryingChangeData.getId())).thenReturn(Optional.empty());
        //
        //Then
        //
        assertThrows(UserNotFoundException.class, changeUserData(userTryingChangeData, userWithAlreadyChangedData));
    }

    @Test
    void shouldNotChangeUserData_correctUserNotAuthenticated() {
        //
        //Given
        //
        UserEntity userTryingChangeData = getSimpleUser();
        UserEntity userWithAlreadyChangedData = getAnotherUserWithSameId();
        UserEntity anotherUser = getAnotherUser();
        //
        //When
        //
        Mockito.when(userRepository.findById(userTryingChangeData.getId())).thenReturn(Optional.of(userTryingChangeData));
        authenticateUser(anotherUser);
        //
        //Then
        //
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertThrows(AccessDeniedException.class, changeUserData(userTryingChangeData, userWithAlreadyChangedData));
    }

    @Test
    void shouldNotChangeUserData_noAuthentication() {
        //
        //Given
        //
        UserEntity userTryingChangeData = getSimpleUser();
        UserEntity userWithAlreadyChangedData = getAnotherUserWithSameId();
        //
        //When
        //
        Mockito.when(userRepository.findById(userTryingChangeData.getId())).thenReturn(Optional.of(userTryingChangeData));
        //
        //Then
        //
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertThrows(AccessDeniedException.class, changeUserData(userTryingChangeData, userWithAlreadyChangedData));
    }

    @Test
    void shouldNotChangeUserData_usernameAlreadyExist() {
        //
        //Given
        //
        UserEntity userTryingChangeData = getSimpleUser();
        UserEntity userWithAlreadyChangedData = getAnotherUserWithSameId();
        //
        //When
        //
        Mockito.when(userRepository.findById(userTryingChangeData.getId())).thenReturn(Optional.of(userTryingChangeData));
        Mockito.when(userRepository.findUserByUsername(userWithAlreadyChangedData.getUsername())).thenReturn(getAnotherUser());
        authenticateUser(userTryingChangeData);
        //
        //Then
        //
        assertThrows(UsernameExistsException.class, changeUserData(userTryingChangeData, userWithAlreadyChangedData));
    }

    @Test
    void shouldNotChangeUserData_emailAlreadyExist() {
        //
        //Given
        //
        UserEntity userTryingChangeData = getSimpleUser();
        UserEntity userWithAlreadyChangedData = getAnotherUserWithSameId();
        //
        //When
        //
        Mockito.when(userRepository.findById(userTryingChangeData.getId())).thenReturn(Optional.of(userTryingChangeData));
        Mockito.when(userRepository.findUserByUsername(userWithAlreadyChangedData.getUsername())).thenReturn(null);
        Mockito.when(userRepository.findUserByEmail(userWithAlreadyChangedData.getEmail())).thenReturn(getAnotherUser());
        authenticateUser(userTryingChangeData);
        //
        //Then
        //
        assertThrows(EmailExistsException.class, changeUserData(userTryingChangeData, userWithAlreadyChangedData));
    }

    @Test
    void shouldNotDeleteUserById_wrongAuthentication() {
        //
        //Given
        //
        UserEntity userToDelete = getSimpleUser();
        UserEntity wrongUserAuthenticated = getAnotherUser();
        //
        //When
        //
        Mockito.when(userRepository.findById(userToDelete.getId())).thenReturn(Optional.of(userToDelete));
        authenticateUser(wrongUserAuthenticated);
        //
        //Then
        //
        assertThrows(AccessDeniedException.class, () -> userService.deleteUserById(userToDelete.getId()));
    }

    @Test
    void shouldNotDeleteUserById_noAuthentication() {
        //
        //Given
        //
        UserEntity userToDelete = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findById(userToDelete.getId())).thenReturn(Optional.of(userToDelete));
        //
        //Then
        //
        assertNull(getAuthentication());
        assertThrows(AccessDeniedException.class, () -> userService.deleteUserById(userToDelete.getId()));
    }

    @Test
    void shouldNotDeleteUserById_userNotExist() {
        //
        //Given
        //
        UserEntity userToDelete = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findById(userToDelete.getId())).thenReturn(Optional.empty());
        //
        //Then
        //
        assertThrows(NoResultException.class, () -> userService.deleteUserById(userToDelete.getId()));
    }

    @Test
    void shouldDeleteUserById() {
        //
        //Given
        //
        UserEntity userToDelete = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findById(userToDelete.getId())).thenReturn(Optional.of(userToDelete));
        authenticateUser(userToDelete);
        userService.deleteUserById(userToDelete.getId());
        //
        //Then
        //
        Mockito.verify(userRepository).delete(userToDelete);
    }

    @Test
    void ShouldDeleteUser() {
        //
        //Given
        //
        UserEntity userToDelete = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findUserByUsername(userToDelete.getUsername())).thenReturn(userToDelete);
        userService.deleteUser(userToDelete.getUsername());
        //
        //Then
        //
        Mockito.verify(userRepository).delete(userToDelete);
    }

    @Test
    void shouldGetCurrentlyRentedBooks() {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        authenticateUser(userEntity);
        //
        //Then
        //
        assertEquals(userEntity.getRentedBooks(), userService.getCurrentlyRentedBooks());
    }

    @Test
    void shouldNotGetCurrentlyRentedBooks_noAuthentication() {
        assertThrows(AccessDeniedException.class, () -> userService.getCurrentlyRentedBooks());
    }

    @Test
    void shouldGetMyHistory() {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        List<HistoryOfBookEntity> listOfHistory = getListOfHistoryOfBookEntity(userEntity);
        List<HistoryOfBookForUserResponse> listOfHistoryForUser = mapHistoryOfBooks(listOfHistory);
        //
        //When
        //
        authenticateUser(userEntity);
        Mockito.when(historyOfBooksRepository.findByUserEntity_username(userEntity.getUsername())).thenReturn(listOfHistory);
        //
        //Then
        //
        assertEquals(listOfHistoryForUser, userService.getMyHistory());
    }

    @Test
    void shouldNotGetMyHistory_wrongAuthentication() {
        assertThrows(AccessDeniedException.class, () -> userService.getMyHistory());
    }

    @Test
    void shouldGetUserHistoryById() throws UserNotFoundException {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        List<HistoryOfBookEntity> listOfHistory = getListOfHistoryOfBookEntity(userEntity);
        List<HistoryOfBookForUserResponse> listOfHistoryForUser = mapHistoryOfBooks(listOfHistory);
        //
        //When
        //
        Mockito.when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        Mockito.when(historyOfBooksRepository.findByUserEntity_id(userEntity.getId())).thenReturn(listOfHistory);
        authenticateUser(userEntity);
        //
        //Then
        //
        assertEquals(listOfHistoryForUser, userService.getUserHistoryById(userEntity.getId()));
    }

    @Test
    void shouldNotGetUserHistoryById_userNotFound() {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findById(userEntity.getId())).thenReturn(Optional.empty());
        //
        //Then
        //
        assertThrows(UserNotFoundException.class, () -> userService.getUserHistoryById(userEntity.getId()));
    }

    @Test
    void shouldNotGetUserHistoryById_wrongAuthentication() {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        //
        //Then
        //
        assertThrows(AccessDeniedException.class, () -> userService.getUserHistoryById(userEntity.getId()));
    }

    @Test
    void shouldNotGetUserHistoryById_wrongUserAuthenticated() {
        //
        //Given
        //
        UserEntity userLoggedIn = getSimpleUser();
        UserEntity userWhomHistoryIsBeingLookedFor = getAnotherUser();
        //
        //When
        //
        Mockito.when(userRepository.findById(userWhomHistoryIsBeingLookedFor.getId())).thenReturn(Optional.of(userWhomHistoryIsBeingLookedFor));
        authenticateUser(userLoggedIn);
        //
        //Then
        //
        assertThrows(AccessDeniedException.class, () -> userService.getUserHistoryById(userWhomHistoryIsBeingLookedFor.getId()));
    }

    @Test
    void shouldNotGetUserHistoryByUsername_userNotFound() {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(null);
        //
        //Then
        //
        assertThrows(UserNotFoundException.class, () -> userService.getUserHistoryByUsername(userEntity.getUsername()));
    }

    @Test
    void shouldNotGetHistoryByUsername_noAuthentication() throws UserNotFoundException {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        //
        //When
        //
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        //
        //Then
        //
        assertThrows(AccessDeniedException.class, () -> userService.getUserHistoryByUsername(userEntity.getUsername()));
    }

    @Test
    void shouldNotGetHistoryByUsername_wrongAuthentication() {
        //
        //Given
        //
        UserEntity userLoggedIn = getSimpleUser();
        UserEntity userWhomHistoryIsBeingLookedFor = getAnotherUser();
        //
        //When
        //
        Mockito.when(userRepository.findUserByUsername(userWhomHistoryIsBeingLookedFor.getUsername())).thenReturn(userWhomHistoryIsBeingLookedFor);
        authenticateUser(userLoggedIn);
        //
        //Then
        //
        assertThrows(AccessDeniedException.class, () -> userService.getUserHistoryByUsername(userWhomHistoryIsBeingLookedFor.getUsername()));
    }

    @Test
    void shouldGetHistoryByUsername() throws UserNotFoundException {
        //
        //Given
        //
        UserEntity userEntity = getSimpleUser();
        List<HistoryOfBookEntity> listOfHistory = getListOfHistoryOfBookEntity(userEntity);
        List<HistoryOfBookForUserResponse> listOfHistoryForUser = mapHistoryOfBooks(listOfHistory);
        //
        //When
        //
        Mockito.when(userRepository.findUserByUsername(userEntity.getUsername())).thenReturn(userEntity);
        Mockito.when(historyOfBooksRepository.findByUserEntity_username(userEntity.getUsername())).thenReturn(listOfHistory);
        authenticateUser(userEntity);
        //
        //Then
        //
        assertEquals(listOfHistoryForUser, userService.getUserHistoryByUsername(userEntity.getUsername()));
    }

    private UserEntity getSimpleUser() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(2L);
        userEntity.setFirstName("John");
        userEntity.setLastName("Smith");
        userEntity.setUsername("john1");
        userEntity.setEmail("john@john.com");
        userEntity.setPassword("John123");
        userEntity.setAuthorities(Role.ROLE_USER.getAuthorities());
        createBookForUser(userEntity);
//        createNotificationsForUser(userEntity);
        return userEntity;
    }

    private UserEntity getAnotherUserWithSameId() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(2L);
        userEntity.setFirstName("John2");
        userEntity.setLastName("Smith2");
        userEntity.setUsername("john12");
        userEntity.setEmail("john@john.com2");
        userEntity.setPassword("John1232");
        userEntity.setAuthorities(Role.ROLE_USER.getAuthorities());
        return userEntity;
    }

    private UserEntity getAnotherUser() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(3L);
        userEntity.setFirstName("John3");
        userEntity.setLastName("Smith3");
        userEntity.setUsername("john13");
        userEntity.setEmail("john@john.com3");
        userEntity.setPassword("John1233");
        userEntity.setAuthorities(Role.ROLE_USER.getAuthorities());
        return userEntity;
    }

    private BookEntity createBookForUser(UserEntity userEntity) {
        BookEntity bookEntity = new BookEntity();
        bookEntity.setId(1L);
        bookEntity.setCurrentUser(userEntity);
        bookEntity.setTitle("Hobbit");
        bookEntity.setAuthor("Tolkien");
        bookEntity.setPresent(false);
//        bookEntity.setDateOfRent(DateUtils.createLocalDate("2022-11-08"));
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName("science-fiction");
        categoryEntity.setId(1L);
        List<CategoryEntity> listOfCategories = new ArrayList<>();
        listOfCategories.add(categoryEntity);
        bookEntity.setCategoriesList(listOfCategories);
        List<BookEntity> listOfBooks = new ArrayList<>();
        listOfBooks.add(bookEntity);
        categoryEntity.setListOfBooks(listOfBooks);
        userEntity.setRentedBooks(listOfBooks);
        return bookEntity;
    }


    private BookEntity createSimpleBook() {
        BookEntity bookEntity = new BookEntity();
        bookEntity.setId(2L);
        bookEntity.setTitle("The returning of the king");
        bookEntity.setAuthor("Tolkien");
        bookEntity.setPresent(true);
        return bookEntity;
    }

    private UserEntity getAdmin() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername(SuperAdminRoleConstants.ADMIN_USERNAME);
        userEntity.setEmail(SuperAdminRoleConstants.ADMIN_EMAIL);
        userEntity.setAuthorities(Role.ROLE_SUPER_ADMIN.getAuthorities());
        return userEntity;
    }

    private HistoryOfBookEntity getHistoryOfBookEntity(UserEntity userEntity) {
        HistoryOfBookEntity historyOfBookEntity = new HistoryOfBookEntity();
        historyOfBookEntity.setId(1L);
        historyOfBookEntity.setUserEntity(userEntity);
        historyOfBookEntity.setBookEntity(createSimpleBook());
        return historyOfBookEntity;
    }

    private List<HistoryOfBookEntity>getListOfHistoryOfBookEntity(UserEntity userEntity) {
        List<HistoryOfBookEntity> list = new ArrayList<>();
        list.add(getHistoryOfBookEntity(userEntity));
        return list;
    }

    private List<HistoryOfBookForUserResponse> mapHistoryOfBooks(List<HistoryOfBookEntity> historyOfBooksByUsername) {
        return historyOfBooksByUsername.stream().map(historyOfBook -> new HistoryOfBookForUserResponse(historyOfBook.getBookEntity())).collect(Collectors.toList());
    }

    private void authenticateUser (UserEntity userEntity){
        UserPrincipal userPrincipal = new UserPrincipal(userEntity);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userPrincipal.getUsername(), null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

    private Executable changeUserData (UserEntity userTryingChangeData, UserEntity userWithAlreadyChangedData) {
        return () -> userService.changeUserData(userTryingChangeData.getId(),
                userWithAlreadyChangedData.getFirstName(), userWithAlreadyChangedData.getLastName(),
                userWithAlreadyChangedData.getUsername(), userWithAlreadyChangedData.getEmail(),
                userWithAlreadyChangedData.getPassword());
    }

    private void checkAllUserPersonalData(UserEntity modelUser, UserEntity userToCompare) {
        assertEquals(modelUser.getUsername(), userToCompare.getUsername());
        assertEquals(modelUser.getFirstName(), userToCompare.getFirstName());
        assertEquals(modelUser.getLastName(), userToCompare.getLastName());
        assertEquals(modelUser.getEmail(), userToCompare.getEmail());
        assertEquals(modelUser.getPassword(), userToCompare.getPassword());
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

//    @BeforeEach
//    void setUp() {
//        this.userBCryptPasswordEncoder = new BCryptPasswordEncoder();
//    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }


}
