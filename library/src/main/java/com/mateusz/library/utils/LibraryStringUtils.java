package com.mateusz.library.utils;

import com.mateusz.library.model.dao.BookEntity;
import com.mateusz.library.model.dao.CategoryEntity;

import java.awt.print.Book;

public class LibraryStringUtils {

    public static String nullSafeToString(Object phrase) {
        if(phrase==null){
            return "";
        }
        return phrase.toString();
    }

    public static boolean isNotBlankAndNull(String string) {
        if (string!=null) {
            return !string.isBlank();
        }
        return false;
    }

    public static String[] getCategoriesFromBookEntity(BookEntity bookEntity) {
        return bookEntity.getCategoriesList().stream().map(CategoryEntity::getName).toArray(String[]::new);
    }
}
