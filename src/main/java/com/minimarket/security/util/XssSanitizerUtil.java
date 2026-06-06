package com.minimarket.security.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class XssSanitizerUtil {

    public static String sanitizeInput(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return Jsoup.clean(input, Safelist.basic());
    }

}
