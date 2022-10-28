package com.mateusz.library.model.dto;

import com.mateusz.library.model.dao.UserEntity;

public class GetBookWithOwnerResponse {

    private Long id;

    private String title;

    private String author;

    private boolean present;

    private UserEntity currentUser;
}
