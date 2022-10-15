package com.mateusz.library.model.dto;

import lombok.Data;

@Data
public class AddUserRequest {

    private String name;

    private String lastName;

    private String email;
}
