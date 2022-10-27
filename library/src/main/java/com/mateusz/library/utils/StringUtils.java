package com.mateusz.library.utils;

public class StringUtils {

    public static String nullSafeToString(Object phrase) {
        if(phrase==null){
            return "";
        }
        return phrase.toString();
    }
}
