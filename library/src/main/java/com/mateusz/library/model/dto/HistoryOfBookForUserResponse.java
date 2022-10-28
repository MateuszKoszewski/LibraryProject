package com.mateusz.library.model.dto;

import com.mateusz.library.model.dao.BookEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class HistoryOfBookForUserResponse {

    private BookEntity historyBook;

}
