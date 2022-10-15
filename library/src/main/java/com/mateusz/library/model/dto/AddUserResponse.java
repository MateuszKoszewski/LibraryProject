package com.mateusz.library.model.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AddUserResponse {

    private String email;
}
