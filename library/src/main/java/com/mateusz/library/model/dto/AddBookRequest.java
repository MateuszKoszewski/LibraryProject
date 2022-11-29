package com.mateusz.library.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class AddBookRequest {

    private String title;

    private String author;

    private String[] categoriesList;

    private BigDecimal price;
}
