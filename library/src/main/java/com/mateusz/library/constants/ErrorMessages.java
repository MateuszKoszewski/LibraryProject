package com.mateusz.library.constants;

import org.springframework.security.core.context.SecurityContextHolder;

public class ErrorMessages {

    public static final String UNAUTHORIZED_USER_TRIED_TO = "unauthorized user " + SecurityContextHolder.getContext().getAuthentication().getPrincipal() + " tried to ";
    public static final String DELETE_USER = "delete user: ";
    public static final String CHANGE_DATA_OF_USER ="change data of user: ";

    public static final String GET_HISTORY_OF_ANOTHER_USER = "get history of another user: ";

    public static final String NO_USER_FOUND_BY_ID = "no user found by id";

    public static final String NO_USER_FOUND_BY_USERNAME = "no user found by username";
}
