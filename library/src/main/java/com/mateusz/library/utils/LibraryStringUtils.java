package com.mateusz.library.utils;

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
}
