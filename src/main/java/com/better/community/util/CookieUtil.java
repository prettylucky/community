package com.better.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/7/9
 */
public class CookieUtil {
    public static String getValue(HttpServletRequest request, String cookieName) {
        if (request == null || cookieName == null) {
            throw new IllegalArgumentException("参数为空！");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
