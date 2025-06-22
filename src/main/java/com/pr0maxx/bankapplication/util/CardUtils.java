package com.pr0maxx.bankapplication.util;

public class CardUtils {
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber.length() < 4) return "****";
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
}
