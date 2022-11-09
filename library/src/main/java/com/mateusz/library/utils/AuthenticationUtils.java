package com.mateusz.library.utils;

import com.mateusz.library.exception.ExceptionHandling;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationUtils {

    public static String getCurrentlyLoggedInUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException(ExceptionHandling.NOT_ENOUGH_PERMISSION);
        }
        return authentication.getPrincipal().toString();
    }

    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
