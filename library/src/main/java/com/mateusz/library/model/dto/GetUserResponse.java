package com.mateusz.library.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class GetUserResponse {

    private long id;

    private String email;

    private String name;

    private String lastName;

    private List<GetBookResponse> listOfBooks;
}
