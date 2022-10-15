package com.mateusz.library.model.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GetBookResponse {

    private long id;

    private String title;

    private String author;

}
