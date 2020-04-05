package com.prc391.patra.utils;

import org.springframework.util.StringUtils;

public class PatraStringUtils extends StringUtils {
    public static boolean isBlankAndEmpty(String str) {
        if (isEmpty(str)) {
            return true;
        }
        if (isEmpty(str.trim())) {
            return true;
        }
        return false;
    }
}
