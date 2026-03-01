package com.harshith.dsa_question_picker.utils;

public class Utility {
    public static boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
