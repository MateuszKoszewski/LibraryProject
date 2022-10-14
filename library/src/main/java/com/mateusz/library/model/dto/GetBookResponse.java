package com.mateusz.library.model.dto;

import lombok.Builder;

@Builder
public class GetBookResponse {

    private Long id;

    private String title;

    private String author;

}
