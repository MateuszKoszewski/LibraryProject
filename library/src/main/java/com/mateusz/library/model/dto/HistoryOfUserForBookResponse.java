package com.mateusz.library.model.dto;

import com.mateusz.library.model.dao.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HistoryOfUserForBookResponse {

    private UserEntity userEntity;
}
